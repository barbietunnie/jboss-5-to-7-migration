package com.pra.rave.jpa.service;

public class NonTransmittedService {

	/*
select srf.* from rave_srf_1 srf, rave_adverse_1 ae
where srf.srcasnum = ae.aecasnum
  and (srf.srtrnid = '' or srf.srtrnid is null)
  and ae.serny = 'Y' 
  and ae.aevt > '' and ae.relny > '' and ae.aesout > '' 
  and (ae.aesdth = 'Yes' or ae.aeslife = 'Yes' or ae.aeshosp = 'Yes' or ae.aesdisab = 'Yes' or ae.aescong = 'Yes' or ae.aesmie = 'Yes')
  and srf.srcasdtl > '' 
  and ae.aestdt is not null and ae.aestdt < SYSDATE -1;
  	 */
	
	/*
select srf.* from rave_srf_1 srf, rave_adverse_1 ae
where srf.srcasnum = ae.aecasnum
  and srf.srtrnid > '' and ae.serny = 'Y' 
  and ae.aevt > '' and ae.relny > '' and ae.aesout > '' 
  and (ae.aesdth = 'Yes' or ae.aeslife = 'Yes' or ae.aeshosp = 'Yes' or ae.aesdisab = 'Yes' or ae.aescong = 'Yes' or ae.aesmie = 'Yes')
  and srf.srcasdtl > '' 
  and ae.updated_dtc > srf.srtrndtc + 1;
	 */
}
