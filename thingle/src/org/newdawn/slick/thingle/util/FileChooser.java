package org.newdawn.slick.thingle.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.filechooser.FileSystemView;

import org.newdawn.slick.thingle.Page;
import org.newdawn.slick.thingle.Theme;
import org.newdawn.slick.thingle.internal.Thinlet;
import org.newdawn.slick.thingle.spi.ThinletException;

/**
 * A file chooser emulating the swing one but rendered using the Thinlet implementation
 * for slick.
 * 
 * @author kevin
 */
public class FileChooser {
	/** The theme applied to the dialog */
	private Theme theme = new Theme();
	/** The page used to render the dialog */
	private Page page;
	/** True if the GUI is visible */
	private boolean visible = false;
	/** The file system view used to safely interegate the file system */
	private FileSystemView view = FileSystemView.getFileSystemView();
	/** The directory the file chooser is pointing at */
	private File currentDir = view.getDefaultDirectory();
	/** The thinlet component for the dialog */
	private Object chooserDialog;
	/** The thinlet component for the list of files */
	private Object filesList;
	/** The thinlet component for the path combo */
	private Object pathCombo;
	/** The thinlet component for the text field */
	private Object field;
	
	/** The list of files shown in the path selection */
	private ArrayList pathShown = new ArrayList();
	/** The list of files shown in the central list view */
	private ArrayList filesListed = new ArrayList();
	
	/** The title to be given to the dialog */
	private String title = "Save";
	/** The listener to be notified of dialog events */
	private FileChooserListener listener;
	/** True if you're allowed to select directories as well as files */
	private boolean directorySelectionAllowed = false;
	/** The label to place on the select button */
	private String select = "Open";
	
	/** The thinlet instance to control */
	private Thinlet thinlet;
	
	/**
	 * Create a new file chooser
	 * 
	 * @param title The title to display in the dialog 
	 * @param select The label to place on the OK button
	 * @param listener The listener to be notified of dialog events
 	 * @throws ThinletException Indicates a failure to load required resources
	 */
	public FileChooser(String title, String select, FileChooserListener listener) throws ThinletException {
		this(title, select, FileSystemView.getFileSystemView().getDefaultDirectory(), listener);
	}

	/**
	 * Create a new file chooser
	 * 
	 * @param currentDir The directory to display the dialog focused on
	 * @param title The title to display in the dialog 
	 * @param select The label to place on the OK button
	 * @param listener The listener to be notified of dialog events
 	 * @throws ThinletException Indicates a failure to load required resources
	 */
	public FileChooser(String title, String select, File currentDir, FileChooserListener listener) throws ThinletException {
		this.listener = listener;
		this.title = title;
		this.currentDir = currentDir;
		
		page = new Page("utilres/filechooser.xml", this);
		page.setTheme(theme);
		page.setDrawDesktop(false);
	}
	
	/**
	 * Set the theme to be used
	 * 
	 * @param theme The theme to be used
	 */
	public void setTheme(Theme theme) {
		this.theme = theme;
		page.setTheme(theme);
	}
	
	/**
	 * Indicate whether the user can select directories
	 * 
	 * @param allowed True if they can select directories
	 */
	public void setDirectorySelectionAllowed(boolean allowed) {
		directorySelectionAllowed = allowed;
	}
	
	/**
	 * THINLET CALLBACK - Don't use.
	 * 
	 * Initialises the select button
	 * 
	 * @param button The button being selected
	 */
	public void initSelect(Object button) {
		thinlet.setString(button, "text", select);
	}
	
	/**
	 * THINLET CALLBACK - Don't use.
	 * 
	 * Initialise the list 
	 * 
	 * @param list The list to initialise
	 */
	public void initList(Object list) {
		this.filesList = list;
	}

	/**
	 * THINLET CALLBACK - Don't use.
	 * 
	 * Initialise the dialog 
	 * 
	 * @param thinlet The thinlet instance being configured
	 * @param dialog The dialog to initialise
	 */
	public void initDialog(Thinlet thinlet, Object dialog) {
		this.chooserDialog = dialog;
		this.thinlet = thinlet;
		
		thinlet.setString(dialog, "text", title);
	}

	/**
	 * THINLET CALLBACK - Don't use.
	 * 
	 * Initialise the field 
	 * 
	 * @param field The field to initialise
	 */
	public void initField(Object field) {
		this.field = field;
	}

	/**
	 * THINLET CALLBACK - Don't use.
	 * 
	 * Invoke the cancel operation, clearing the dialog and
	 * reporting the cancellation.
	 */
	public void cancel() {
		page.disable();
		visible = false;
		thinlet.remove(chooserDialog);
		
		listener.chooserCanceled();
	}
	
	/**
	 * Invoke selection of the file notifying the listener
	 * 
	 * @param file The file that has been selected
	 */
	private void selectFile(File file) {
		page.disable();
		visible = false;
		thinlet.remove(chooserDialog);
		
		listener.fileSelected(file);
	}
	
	/**
	 * THINLET CALLBACK - Don't use.
	 * 
	 * An entry has been selected from the GUI or by typing it in
	 * 
	 * @param source The component the event came from
	 */
	public void selectEntry(Object source) {
		boolean doubleClick = source == filesList;
		
		for (int i = thinlet.getCount(filesList) - 1; i >= 0; i--) {
			Object item = thinlet.getItem(filesList, i);
			if (thinlet.getBoolean(item, "selected")) {
				// selected item
				File file = (File) filesListed.get(i);
				if (file.isDirectory() && (doubleClick || !directorySelectionAllowed)) {
					currentDir = file;
					populateChooser(pathCombo);
				} else {
					selectFile(file);
				}
				return;
			}
		}
	}
	

	/**
	 * THINLET CALLBACK - Don't use.
	 * 
	 * Fill the file list with data
	 */
	public void populateList() {
		Object fileList = filesList;
		thinlet.removeAll(fileList);
		
		File[] files = currentDir.listFiles();
		filesListed.clear();
		
		ArrayList allFiles = new ArrayList();
		for (int i=0;i<files.length;i++) {
			allFiles.add(files[i]);
		}
		
		Collections.sort(allFiles, new Comparator() {

			public int compare(Object arg0, Object arg1) {
				File file1 = (File) arg0;
				File file2 = (File) arg1;
				
				if (file1.isDirectory() && !file2.isDirectory()) {
					return -1;
				}
				if (file2.isDirectory() && !file1.isDirectory()) {
					return 1;
				}
				
				String name1 = view.getSystemDisplayName(file1);
				String name2 = view.getSystemDisplayName(file2);
				
				if (name1.startsWith(".") && !name2.startsWith(".")) {
					return -1;
				}
				if (!name1.startsWith(".") && name2.startsWith(".")) {
					return 1;
				}
				if ((file1.getName().length() == 0) && (file2.getName().length() != 0)) {
					return -1;
				}
				if ((file2.getName().length() == 0) && (file1.getName().length() != 0)) {
					return 1;
				}
				
				return name1.compareToIgnoreCase(name2);
			}
			
		});
			
		for (int i=0;i<allFiles.size();i++) {
//			if (view.isHiddenFile(files[i])) {
//				continue;
//			}
			File file = (File) allFiles.get(i);
			
			Object item = Thinlet.create("item");
			String text = view.getSystemDisplayName(file);
			thinlet.setString(item, "text", text);
			filesListed.add(file);
				
			if (file.isDirectory()) {
				if (file.getName().equals("")) {
					thinlet.setIcon(item, "icon", thinlet.getIcon("utilres/disk.gif"));
				} else {
					thinlet.setIcon(item, "icon", thinlet.getIcon("utilres/folder.gif"));
				}
			} else {
				thinlet.setIcon(item, "icon", thinlet.getIcon("utilres/document.gif"));
			}
			
			thinlet.add(filesList, item);
		}
	}
	

	/**
	 * THINLET CALLBACK - Don't use.
	 * 
	 * Go up a directory.
	 */
	public void goUp() {
		File parent = view.getParentDirectory(currentDir);
		if (parent != null) {
			currentDir = parent;
			populateChooser(pathCombo);
		}
	}

	/**
	 * THINLET CALLBACK - Don't use.
	 * 
	 * Notification that an file has been selected from the list
	 * 
	 * @param list The list selected from
	 */
	public void enterEntry(Object list) {
		for (int i = thinlet.getCount(filesList) - 1; i >= 0; i--) {
			Object item = thinlet.getItem(filesList, i);
			if (thinlet.getBoolean(item, "selected")) {
				File file = (File) filesListed.get(i);
				if ((!file.isDirectory() || directorySelectionAllowed)) {
					thinlet.setString(field, "text", file.getName());
				}
			}
		}
	}

	/**
	 * Provess the entry of a file location
	 * 
	 * @param file The location provided
	 */
	private void processFileEntry(File file) {
		if (file.isDirectory()) {
			currentDir = file;
			populateChooser(pathCombo);
			thinlet.setString(field, "text", "");
		} else {
			selectFile(file);
			thinlet.setString(field, "text", "");
		}
	}

	/**
	 * THINLET CALLBACK - Don't use.
	 * 
	 * Notification that an file has been selected by entering it's name
	 * 
	 * @param field The field that the text was entered into
	 */
	public void enterText(Object field) {
		String text = thinlet.getString(field, "text");
		if (text.trim().length() == 0) {
			return;
		}
		
		File local = new File(currentDir, text);
		if (local.exists()) {
			processFileEntry(local);
			return;
		}
		File abs = new File(text);
		if (abs.exists()) {
			processFileEntry(abs);
			return;
		}
	}
	
	/**
	 * THINLET CALLBACK - Don't use.
	 * 
	 * Update the chooser combo
	 * 
	 * @param combo The combo to be updated
	 */
	public void changeChooser(Object combo) {
		int index = thinlet.getInteger(combo, "selected");
		currentDir = (File) pathShown.get(index);
		
		populateChooser(combo);
	}

	/**
	 * THINLET CALLBACK - Don't use.
	 * 
	 * Fill the combo with fresh data based on the current directory
	 * 
	 * @param combo The combo to be updated
	 */
	public void populateChooser(Object combo) {
		this.pathCombo = combo;
		
		pathShown.clear();
		thinlet.removeAll(combo);
		
		File[] files = view.getRoots();
		
		ArrayList path = new ArrayList();
		File temp = currentDir;
		while (!view.isRoot(temp)) {
			path.add(0, temp);
			temp = view.getParentDirectory(temp);
			if (temp == null) {
				break;
			}
		}
		
		if (temp != null) {
			path.add(0, temp);
		}
		
		int index = 0;
		boolean displayed = false;
		for (int i=0;i<files.length;i++) {
			Object item = Thinlet.create("choice");
			thinlet.setString(item, "text", view.getSystemDisplayName(files[i]));
			
			thinlet.add(combo, item);
			pathShown.add(files[i]);
			if (currentDir.equals(files[i])) {
				thinlet.setInteger(combo, "selected", index);
			}
			index++;

			if (path.get(0).equals(files[i])) {
				displayed = true;
				String prefix = "  ";
				for (int j=1;j<path.size();j++) {
					item = Thinlet.create("choice");
					String entry = view.getSystemDisplayName((File) path.get(j));
					thinlet.setString(item, "text", prefix + entry);
					thinlet.add(combo, item);
					pathShown.add((File) path.get(j));
					prefix += "  ";
					
					thinlet.setInteger(combo, "selected", index);
					thinlet.setString(combo, "text", entry);
					index++;
				}
			}
		}
		
		if (!displayed) {
			String prefix = "  ";
			for (int j=0;j<path.size();j++) {
				Object item = Thinlet.create("choice");
				String entry = view.getSystemDisplayName((File) path.get(j));
				thinlet.setString(item, "text", prefix + entry);
				thinlet.add(combo, item);
				pathShown.add((File) path.get(j));
				prefix += "  ";
				
				thinlet.setInteger(combo, "selected", index);
				thinlet.setString(combo, "text", entry);
				index++;
			}
		}
		
		populateList();
	}
	
	/**
	 * Show the dialog
	 */
	public void show() {
		visible = true;
		page.enable();
	}
	
	/**
	 * Render the dialog - should be called in the game loop
	 */
	public void render() {
		if (visible) {
			page.render();
		}
	}
	
	/**
	 * Check if the dialog is visible
	 * 
	 * @return True if the dialog is visible
	 */
	public boolean isVisible() {
		return visible;
	}
}