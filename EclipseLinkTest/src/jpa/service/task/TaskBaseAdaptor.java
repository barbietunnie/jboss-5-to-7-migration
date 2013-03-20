package jpa.service.task;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public abstract class TaskBaseAdaptor implements TaskBaseBo {

	public List<String> getArgumentList(String taskArguments) {
		ArrayList<String> list = new ArrayList<String>();
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
