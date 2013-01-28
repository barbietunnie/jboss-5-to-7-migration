package com.pra.rave.ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.cdisc.ns.odm.v1.ODM;
import org.cdisc.ns.odm.v1.ODMcomplexTypeDefinitionClinicalData;
import org.cdisc.ns.odm.v1.ODMcomplexTypeDefinitionSubjectData;
import org.slf4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.pra.rave.jpa.model.Study;
import com.pra.rave.jpa.model.StudyPK;
import com.pra.rave.jpa.model.Subject;
import com.pra.rave.jpa.service.StudyService;
import com.pra.rave.jpa.util.FormOid;
import com.pra.rave.jpa.util.SpringUtil;
import com.pra.rave.xml.util.StringUtil;
import com.pra.util.logger.LoggerHelper;

public class RaveDataLoader {
	static Logger logger = LoggerHelper.getLogger();
	
	public static void main(String[] args) {
		RaveDataLoader loader = new RaveDataLoader();
		RaveClinicalView client = new RaveClinicalView();
		try {
			loader.persistForm("ADVERSE_1", client);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}

	private PlatformTransactionManager txmgr;
	private TransactionStatus status;

	private void prepare() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("rave_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("raveTransactionManager");
		status = txmgr.getTransaction(def);
	}

	private void teardown() {
		txmgr.rollback(status);
	}

	public void persistForm(String formOid, RaveClinicalView client) {
		ODM odm = client.getClinicalViewByForm(formOid);
		if (odm == null) {
			throw new IllegalArgumentException("FormOid (" + formOid + ") is invalid.");
		}
		prepare();
		try {
			StudyService studyService = (StudyService) SpringUtil.getAppContext().getBean("studyService");
			Study study = new Study();
			XMLGregorianCalendar cal = odm.getCreationDateTime();
			java.sql.Timestamp loadDt = new java.sql.Timestamp(cal.getMillisecond());
			if (FormOid.ADVERSE_1.getValue().equals(formOid)) {
				study.setAdverse1LoadDt(loadDt);
			}
			else if (FormOid.AESCR_1.getValue().equals(formOid)) {
				study.setAescr1LoadDt(loadDt);
			}
			else if (FormOid.SRF_1.getValue().equals(formOid)) {
				study.setSrf1LoadDt(loadDt);
			}
			for (ODMcomplexTypeDefinitionClinicalData clinical : odm.getClinicalData()) {
				StudyPK pk = new StudyPK(clinical.getStudyOID());
				study.setStudyPK(pk);
				study.setMetaDataVersionOID(clinical.getMetaDataVersionOID());
				studyService.insert(study);
				if (study.getSubjects()==null) {
					List<Subject> lst = new ArrayList<Subject>();
					study.setSubjects(lst);
				}
				for (ODMcomplexTypeDefinitionSubjectData subject : clinical.getSubjectData()) {
					Subject subj = new Subject();
					subj.setStudy(study);
					subj.setSubjectKey(subject.getSubjectKey());
					if (subject.getSiteRef()!=null) {
						subj.setLocationOID(subject.getSiteRef().getLocationOID());
					}
					study.getSubjects().add(subj);
					studyService.update(study);
					//txmgr.commit(status);
				}
				logger.debug(StringUtil.prettyPrint(study));
			}
		}
		finally {
			teardown();
		}
 	}

}
