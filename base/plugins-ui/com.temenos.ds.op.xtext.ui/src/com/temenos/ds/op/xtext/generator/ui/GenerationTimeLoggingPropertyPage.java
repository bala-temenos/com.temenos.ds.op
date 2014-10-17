package com.temenos.ds.op.xtext.generator.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.dialogs.EmptyPropertyPage;

@SuppressWarnings("restriction")
public class GenerationTimeLoggingPropertyPage extends EmptyPropertyPage {
	
	private TableViewer tableViewer;
	
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		GridLayout layout = new GridLayout(1,false);
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite tableCompsoite = new Composite(parent, SWT.NONE);
		tableCompsoite.setLayout(new GridLayout(1, false));
		tableCompsoite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		tableViewer = new TableViewer(tableCompsoite);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

		TableViewerColumn firstColumnViewer = createTableViewerColumn(tableViewer, "Generator ID", new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof GeneratorTimeModel) {
					GeneratorTimeModel item = (GeneratorTimeModel) element;
					return item.id;
				}
				return super.getText(element);
			}
		});
		firstColumnViewer.getColumn().setWidth(400);
		firstColumnViewer.getColumn().setToolTipText("MultiGenerators ID");

		TableViewerColumn secondColumnViewer = createTableViewerColumn(tableViewer, "ms", new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof GeneratorTimeModel) {
					GeneratorTimeModel item = (GeneratorTimeModel) element;
					return Integer.toString(item.timeConsumed);
				}
				return super.getText(element);
			}
		});
		secondColumnViewer.getColumn().setWidth(50);
		secondColumnViewer.getColumn().setToolTipText("Total time spent(in ms),since session start or reset");

		TableViewerColumn thirdColumnViewer =createTableViewerColumn(tableViewer, "#", new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof GeneratorTimeModel) {
					GeneratorTimeModel item = (GeneratorTimeModel) element;
					return Integer.toString(item.resourcesProcessed);
				}
				return super.getText(element);
			}
		});
		thirdColumnViewer.getColumn().setWidth(50);
		thirdColumnViewer.getColumn().setToolTipText("Total number of resoures processed,since session start or reset");
		
		tableViewer.setInput(createModel());
		
		//create a reset button
		createResetButton(parent);
		return parent;
	}

	private void createResetButton(Composite parent) {
		Button resetButton = new Button(parent, SWT.PUSH);
		resetButton.setText("Reset");
		
		Dialog.applyDialogFont(resetButton);
		GridData data = new GridData();
		data.horizontalAlignment = SWT.END;
				
		Point minButtonSize = resetButton.computeSize(SWT.DEFAULT,
				SWT.DEFAULT, true);
		data.widthHint = Math.max(convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH), minButtonSize.x);
		resetButton.setLayoutData(data);
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GenerationTimeLogger.getInstance().reset();
				tableViewer.setInput(createModel());
				tableViewer.refresh();
			}
		});
	}
	
	private static class GeneratorTimeModel {
		private String id;
		private int timeConsumed;
		private int resourcesProcessed;

		public GeneratorTimeModel(String id, int timeConsumed,int resourcesProcessed) {
			super();
			this.id = id;
			this.timeConsumed = timeConsumed;
			this.resourcesProcessed = resourcesProcessed;
		}
	}
	
	private List<GeneratorTimeModel> createModel(){
		List<GeneratorTimeModel> models = new ArrayList<GeneratorTimeModel>();
		IMemento memento = GenerationTimeLogger.getInstance().getMemento();
		if(memento != null){
			for(IMemento generator : memento.getChildren()){
				models.add(new GeneratorTimeModel(generator.getType(),generator.getInteger(GenerationTimeLogger.TIME),generator.getInteger(GenerationTimeLogger.COUNT)));
			}
		}
		return models;
	}
	
	private TableViewerColumn createTableViewerColumn(TableViewer viewer, String string, CellLabelProvider labelProvider) {
		TableViewerColumn viewerNameColumn = new TableViewerColumn(viewer, SWT.NONE);
		viewerNameColumn.getColumn().setText(string);
		viewerNameColumn.setLabelProvider(labelProvider);
		return viewerNameColumn;
	}
}
