package org.beryl.widget;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.beryl.diagnostics.Log;
import org.beryl.diagnostics.Logger;

import android.view.View;

public class GenericViewBinder implements ViewBindable {

	final Class<?> objectClass;
	final Object object;
	final View rootView;
	final Class<?> rDotId;
	
	GenericViewBinder(Object object, View rootView, Class<?> rDotId) {
		this.object = object;
		objectClass = this.object.getClass();
		this.rootView = rootView;
		this.rDotId = rDotId;
	}
	
	protected View getRootView() {
		return rootView;
	}
	
	protected Object getObject() {
		return objectClass;
	}

	protected Class<?> getType() {
		return objectClass;
	}
	
	protected Class<?> getRdotId() {
		return this.rDotId;
	}
	
	static class ViewData {
		final Field field;
		public ViewData(Field field) {
			this.field = field;
			this.field.setAccessible(true);
		}
	}
	
	static class RdotIdData {
		final Class<?> rDotId;
		public RdotIdData(Class<?> rDotId) {
			this.rDotId = rDotId;
		}
		
		public int getId(String idName) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
			int id = 0;
			Field idField = this.rDotId.getDeclaredField(idName);
			idField.setAccessible(true);
			Integer objId = (Integer)idField.get(null);
			id = objId.intValue();
			return id;
		}
	}
	
	public List<ViewData> getViews() {
		return getViews(Logger.newInstance("ViewBinder"));
	}
	
	public List<ViewData> getViews(final Log log) {
		final Class<?> myType = getType();
		
		
		ArrayList<ViewData> views = new ArrayList<ViewData>();
		Field[] fields = myType.getDeclaredFields();
		
		for(Field field : fields) {
			field.setAccessible(true);
			Class<?> fieldType = field.getType();
			if(View.class.isAssignableFrom(fieldType)) {
				try {
					views.add(new ViewData(field));
				} catch(Exception e){
					log.e("Failed to assign field=" + field.getName());
					log.e(e);
				}
			}
		}
		
		return views;
	}
	
	public void bindViews() {
		bindViews(Logger.newInstance("ViewBinder"));
	}
	
	public void bindViews(final Log log) {
		final View root = getRootView();
		List<ViewData> viewDatas = getViews();
		RdotIdData rIds = new RdotIdData(getRdotId());

		for(ViewData viewData : viewDatas) {
			String viewName = "<Unknown>";
			try {
				viewName = viewData.field.getName();
				final int rId = rIds.getId(viewName);
				bindView(root, viewData, rId);
			} catch(Exception e) {
				log.e("Failed to bind on View " + viewName);
				log.e(e);
			}
		}
	}
	
	private void bindView(View root, ViewData viewData, int rId) throws IllegalArgumentException, IllegalAccessException {
		final Object myObject = getObject();
		final View foundView = root.findViewById(rId);
		viewData.field.set(myObject, foundView);
	}
}
