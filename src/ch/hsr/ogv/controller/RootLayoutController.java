package ch.hsr.ogv.controller;

import java.io.File;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ch.hsr.ogv.dataaccess.UserPreferences;
import ch.hsr.ogv.model.Endpoint;
import ch.hsr.ogv.model.ModelBox;
import ch.hsr.ogv.model.ModelClass;
import ch.hsr.ogv.model.ModelObject;
import ch.hsr.ogv.view.Arrow;
import ch.hsr.ogv.view.Floor;
import ch.hsr.ogv.view.PaneBox;
import ch.hsr.ogv.view.Selectable;
import ch.hsr.ogv.view.SubSceneAdapter;
import ch.hsr.ogv.view.SubSceneCamera;
import ch.hsr.ogv.view.TSplitMenuButton;

/**
 * The controller for the root layout. The root layout provides the basic application layout containing a menu bar and space where other JavaFX elements can be placed.
 *
 * @author Simon Gwerder
 */
public class RootLayoutController implements Observer, Initializable {

	private Stage primaryStage;
	private String appTitle;

	private ModelViewConnector mvConnector;
	private SubSceneAdapter subSceneAdapter;
	private SelectionController selectionController;
	private CameraController cameraController;

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.appTitle = primaryStage.getTitle();
	}

	public void setMVConnector(ModelViewConnector mvConnector) {
		this.mvConnector = mvConnector;
	}

	public void setSubSceneAdapter(SubSceneAdapter subSceneAdapter) {
		this.subSceneAdapter = subSceneAdapter;
	}

	public void setSelectionController(SelectionController selectionController) {
		this.selectionController = selectionController;
	}

	public void setCameraController(CameraController cameraController) {
		this.cameraController = cameraController;
	}

	/**
	 * Creates an empty view.
	 */
	@FXML
	private void handleNew() {
		this.primaryStage.setTitle(this.appTitle);// set new app title TODO
	}

	/**
	 * Opens a FileChooser to let the user select an address book to load.
	 */
	@FXML
	private void handleOpen() {
		FileChooser fileChooser = new FileChooser();

		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("OGV (*.ogv)", "*.ogv");
		fileChooser.getExtensionFilters().add(extFilter);
		File previousFile = UserPreferences.getSavedFile();
		if (previousFile != null && previousFile.getParentFile() != null && previousFile.getParentFile().isDirectory()) {
			fileChooser.setInitialDirectory(previousFile.getParentFile());
		}
		// Show open file dialog
		File file = fileChooser.showOpenDialog(this.primaryStage);
		if (file != null) {
			this.primaryStage.setTitle(this.appTitle + " - " + file.getName()); // set new app title
			// TODO
		}
	}

	/**
	 * Saves the file to the ogv file that is currently open. If there is no open file, the "save as" dialog is shown.
	 */
	@FXML
	private void handleSave() {
		File file = UserPreferences.getSavedFile();
		if (file != null) {
			// TODO
		} else {
			handleSaveAs();
		}
	}

	/**
	 * Opens a FileChooser to let the user select a file to save to.
	 */
	@FXML
	private void handleSaveAs() {
		FileChooser fileChooser = new FileChooser();

		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("OGV (*.ogv)", "*.ogv");
		fileChooser.getExtensionFilters().add(extFilter);
		File previousFile = UserPreferences.getSavedFile();
		if (previousFile != null && previousFile.getParentFile() != null && previousFile.getParentFile().isDirectory()) {
			fileChooser.setInitialDirectory(previousFile.getParentFile());
		}
		// Show save file dialog
		File file = fileChooser.showSaveDialog(this.primaryStage);

		if (file != null) {
			// Make sure it has the correct extension
			if (!file.getPath().endsWith(".ogv")) {
				file = new File(file.getPath() + ".ogv");
			}
			UserPreferences.setSavedFilePath(file);
			this.primaryStage.setTitle(this.appTitle + " - " + file.getName()); // set new app title
			// TODO
		}
	}

	/**
	 * Opens an about dialog.
	 */
	@FXML
	private void handleAbout() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("About");
		alert.setHeaderText("About");
		alert.setContentText("Authors: Simon Gwerder, Adrian Rieser");
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image("file:resources/images/application_icon.gif")); // add a custom icon
		// alert.initOwner(this.stageManager.getPrimaryStage());
		alert.showAndWait();
	}

	/**
	 * Closes the application.
	 */
	@FXML
	private void handleExit() {
		Platform.exit();
	}

	@FXML
	MenuItem centerView;

	@FXML
	CheckMenuItem lockedTopView;

	@FXML
	CheckMenuItem showObjects;

	@FXML
	CheckMenuItem showModelAxis;

	@FXML
	private void handleCenterView() {
		if (this.cameraController != null) {
			SubSceneCamera ssCamera = this.subSceneAdapter.getSubSceneCamera();
			this.cameraController.handleCenterView(ssCamera);
		}
	}

	@FXML
	private void handleLockedTopView() {
		if (this.cameraController != null) {
			SubSceneCamera ssCamera = this.subSceneAdapter.getSubSceneCamera();
			this.cameraController.handleLockedTopView(ssCamera, this.lockedTopView.isSelected());
		}
	}

	@FXML
	private void handleShowObjects() {
		if (this.showObjects.isSelected()) {
			this.createObject.setDisable(false);
		} else {
			this.createObject.setDisable(true);
		}

		for (ModelBox modelBox : this.mvConnector.getBoxes().keySet()) {
			if (modelBox instanceof ModelObject) {
				PaneBox paneBox = this.mvConnector.getPaneBox(modelBox);
				paneBox.setVisible(this.showObjects.isSelected());

				if (paneBox.isSelected() && this.selectionController != null) {
					this.selectionController.setSelected(this.subSceneAdapter, true, this.subSceneAdapter);
				}

				for (Endpoint endpoint : modelBox.getEndpoints()) {
					Arrow arrow = this.mvConnector.getArrow(endpoint.getRelation());
					arrow.setVisible(this.showObjects.isSelected());
					if (arrow.isSelected() && this.selectionController != null) {
						this.selectionController.setSelected(this.subSceneAdapter, true, this.subSceneAdapter);
					}
				}
			}
		}
	}

	@FXML
	private void handleShowModelAxis() {
		if (this.subSceneAdapter != null) {
			Group axis = this.subSceneAdapter.getAxis();
			if (this.showModelAxis.isSelected()) {
				axis.setVisible(true);
			} else {
				axis.setVisible(false);
			}
		}
	}

	@FXML
	private CheckMenuItem modena;

	@FXML
	private CheckMenuItem caspian;

	@FXML
	private CheckMenuItem aqua;

	@FXML
	private ToggleGroup createToolbar;

	@FXML
	private ToggleButton createClass;

	@FXML
	private Button createObject;

	@FXML
	private SplitMenuButton createRelation;
	private TSplitMenuButton tSplitMenuButton;

	@FXML
	private MenuItem createUndirectedAssociation;

	@FXML
	private MenuItem createDirectedAssociation;

	@FXML
	private MenuItem createBidirectedAssociation;

	@FXML
	private MenuItem createUndirectedAggregation;

	@FXML
	private MenuItem createDirectedAggregation;

	@FXML
	private MenuItem createUndirectedComposition;

	@FXML
	private MenuItem createDirectedComposition;

	@FXML
	private ToggleButton createGeneralization;

	@FXML
	private ToggleButton createDependency;

	@FXML
	Button deleteSelected;

	@FXML
	ColorPicker colorPick;

	@FXML
	private void handleCreateClass() {
		if (this.subSceneAdapter != null) {
			this.subSceneAdapter.onlyFloorMouseEvent(this.createClass.isSelected());
		}
	}

	@FXML
	private void handleCreateObject() {
		this.createToolbar.selectToggle(null);
		Selectable selected = this.selectionController.getSelected();
		if (this.selectionController.hasSelection() && selected instanceof PaneBox && mvConnector.getModelBox((PaneBox) selected) instanceof ModelClass) {
			PaneBox newPaneBox = this.mvConnector.handleCreateNewObject(selected);
			if (newPaneBox != null) {
				this.selectionController.setSelected(newPaneBox, true, this.subSceneAdapter);
			}
		}
	}

	private void splitMenuButtonSelect(MenuItem choosenItem) {
		this.tSplitMenuButton.setChoice(choosenItem);
		this.createToolbar.selectToggle(this.tSplitMenuButton);
	}

	@FXML
	private void handleCreateAssociation() {
		if (tSplitMenuButton.isSelected()) {
			this.createToolbar.selectToggle(null);
		} else {
			this.createToolbar.selectToggle(this.tSplitMenuButton);
		}
	}

	@FXML
	private void handleCreateUndirectedAssociation() {
		splitMenuButtonSelect(this.createUndirectedAssociation);
	}

	@FXML
	private void handleCreateDirectedAssociation() {
		splitMenuButtonSelect(this.createDirectedAssociation);
	}

	@FXML
	private void handleCreateBidirectedAssociation() {
		splitMenuButtonSelect(this.createBidirectedAssociation);
	}

	@FXML
	private void handleCreateUndirectedAggregation() {
		splitMenuButtonSelect(this.createUndirectedAggregation);
	}

	@FXML
	private void handleCreateDirectedAggregation() {
		splitMenuButtonSelect(this.createDirectedAggregation);
	}

	@FXML
	private void handleCreateUndirectedComposition() {
		splitMenuButtonSelect(this.createUndirectedComposition);
	}

	@FXML
	private void handleCreateDirectedComposition() {
		splitMenuButtonSelect(this.createDirectedComposition);
	}

	@FXML
	private void handleCreateGeneralization() {
		// TODO
	}

	@FXML
	private void handleCreateDependency() {
		// TODO
	}

	@FXML
	private void handleDeleteSelected() {
		this.createToolbar.selectToggle(null);
		Selectable selected = this.selectionController.getSelected();
		if (this.selectionController.hasSelection()) {
			this.mvConnector.handleDelete(selected);
			this.selectionController.setSelected(this.subSceneAdapter, true, this.subSceneAdapter);
		}
	}

	@FXML
	private void handleColorPick() {
		this.createToolbar.selectToggle(null);
		Selectable selected = this.selectionController.getSelected();
		if (this.selectionController.hasSelection()) {
			this.mvConnector.handleColorPick(selected, this.colorPick.getValue());
		}
	}

	private void addButtonAccelerators() {
		if (this.createClass != null && this.createObject != null && this.deleteSelected != null) {
			Platform.runLater(() -> {
				this.primaryStage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.C), () -> {
					if (!this.createClass.isDisable()) {
						this.createClass.requestFocus();
						this.createClass.fire();
					}
				});

				this.primaryStage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.O), () -> {
					if (!this.createObject.isDisable()) {
						this.createObject.fire();
					}
				});

				this.primaryStage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.DELETE), () -> {
					if (!this.deleteSelected.isDisable()) {
						this.deleteSelected.fire();
					}
				});
			});
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof SelectionController && arg instanceof Floor && this.selectionController != null) {
			this.subSceneAdapter.onlyFloorMouseEvent(false);
			if (createClass != null && createClass.isSelected()) {
				PaneBox newPaneBox = this.mvConnector.handleCreateNewClass(this.selectionController.getSelectionCoordinates());
				this.createClass.setSelected(false);
				this.selectionController.setSelected(newPaneBox, true, this.subSceneAdapter);
			}
		} else if (o instanceof SelectionController && (arg instanceof PaneBox || arg instanceof Arrow) && this.selectionController != null) {
			Selectable selected = this.selectionController.getSelected();
			if (this.selectionController.hasSelection() && selected instanceof PaneBox && mvConnector.getModelBox((PaneBox) selected) instanceof ModelClass && createObject != null) {
				this.createObject.setDisable(false);
			} else {
				this.createObject.setDisable(true);
			}
			if (this.selectionController.hasSelection()) {
				this.deleteSelected.setDisable(false);
				this.colorPick.setDisable(false);
				if (selected instanceof PaneBox) {
					PaneBox selectedPaneBox = (PaneBox) selected;
					this.colorPick.setValue(selectedPaneBox.getColor());
				} else if (selected instanceof Arrow) {
					Arrow selectedArrow = (Arrow) selected;
					this.colorPick.setValue(selectedArrow.getColor());
				}
			} else {
				this.deleteSelected.setDisable(true);
				this.colorPick.setDisable(true);
				this.colorPick.setValue(Color.WHITE);
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) { // called once FXML is loaded and all fields injected
		addButtonAccelerators();
		this.tSplitMenuButton = new TSplitMenuButton(this.createRelation, this.createUndirectedAssociation, this.createToolbar);
		this.colorPick.getCustomColors().add(PaneBox.DEFAULT_COLOR);
	}
}
