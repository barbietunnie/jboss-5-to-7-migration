package com.es.bo.task;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public abstract class TaskBaseAdaptor implements AbstractTaskBo, java.io.Serializable {
	private static final long serialVersionUID = -8159784802650360342L;

	public List<String> getArgumentList(String taskArguments) {
		List<String> list = new ArrayList<String>();
		if (taskArguments != null) {
			StringTokenizer st = new StringTokenizer(taskArguments, ",");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token != null && token.trim().length() > 0)
					list.add(token);
			}
		}
		return list;
	}
}
