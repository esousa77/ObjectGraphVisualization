package ch.hsr.ogv.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.ogv.controller.ThemeMenuController.Style;
import ch.hsr.ogv.model.Endpoint;
import ch.hsr.ogv.model.Instance;
import ch.hsr.ogv.model.ModelBox;
import ch.hsr.ogv.model.ModelBox.ModelBoxChange;
import ch.hsr.ogv.model.ModelClass;
import ch.hsr.ogv.model.ModelManager;
import ch.hsr.ogv.model.Relation;
import ch.hsr.ogv.model.RelationType;
import ch.hsr.ogv.util.ColorUtil;
import ch.hsr.ogv.util.ResourceLocator;
import ch.hsr.ogv.util.ResourceLocator.Resource;
import ch.hsr.ogv.view.Arrow;
import ch.hsr.ogv.view.PaneBox;
import ch.hsr.ogv.view.SubSceneAdapter;
import ch.hsr.ogv.view.SubSceneCamera;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * 
 * @author Simon Gwerder
 *
 */
public class StageManager extends Observable implements Observer {

	private final static Logger logger = LoggerFactory.getLogger(StageManager.class);

	private String appTitle = "Object Graph Visualizer";
	private Stage primaryStage;
	private BorderPane rootLayout;
	private SubSceneAdapter subSceneAdpater;

	private ModelManager modelManager;
	private Map<ModelBox, PaneBox> boxes = new HashMap<ModelBox, PaneBox>();
	private Map<Relation, Arrow> arrows = new HashMap<Relation, Arrow>();

	private ThemeMenuController themeMenuController = new ThemeMenuController();
	private CameraController cameraController = new CameraController();
	private SubSceneController subSceneController = new SubSceneController();
	private SelectionController selectionController = new SelectionController();
	private TextInputController textInputController = new TextInputController();
	private DragMoveController dragMoveController = new DragMoveController();
	private DragResizeController dragResizeController = new DragResizeController();

	private static final int MIN_WIDTH = 1024;
	private static final int MIN_HEIGHT = 768;

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public String getAppTitle() {
		return appTitle;
	}

	public void setAppTitle(String appTitle) {
		this.appTitle = appTitle;
		this.getPrimaryStage().setTitle(this.appTitle);
	}

	public StageManager(Stage primaryStage) {
		if (primaryStage == null)
			throw new IllegalArgumentException("The primaryStage argument can not be null!");
		this.primaryStage = primaryStage;

		this.modelManager = new ModelManager();
		this.modelManager.addObserver(this);

		initRootLayoutController();
		setupStage();
		initSubSceneController();
		initCameraController();
		initPaneBoxController();
	}

	private void setupStage() {
		this.setAppTitle(this.appTitle);
        this.primaryStage.setMinWidth(MIN_WIDTH);
        this.primaryStage.setMinHeight(MIN_HEIGHT);
        this.primaryStage.getIcons().add(new Image(ResourceLocator.getResourcePath(Resource.ICON_PNG).toExternalForm())); // set the application icon
        
        Pane canvas = (Pane) this.rootLayout.getCenter();
        this.subSceneAdpater = new SubSceneAdapter(canvas.getWidth(), canvas.getHeight());
        SubScene subScene = this.subSceneAdpater.getSubScene();
        canvas.getChildren().add(subScene);
        subScene.widthProperty().bind(canvas.widthProperty());
        subScene.heightProperty().bind(canvas.heightProperty());
        
        Scene scene = new Scene(this.rootLayout);
        this.primaryStage.setScene(scene);
        this.primaryStage.show();
        this.subSceneAdpater.getSubScene().requestFocus();
        
        setChanged();
        notifyObservers(this); // pass StageManager to RootLayoutController
        
        //TODO: Remove everything below this line:
        ModelClass mcA = this.modelManager.createClass("A", new Point3D(100, PaneBox.INIT_DEPTH / 2, 100), PaneBox.MIN_WIDTH, PaneBox.MIN_HEIGHT, PaneBox.DEFAULT_COLOR);
        ModelClass mcB = this.modelManager.createClass("B", new Point3D(300, PaneBox.INIT_DEPTH / 2, 300), PaneBox.MIN_WIDTH, PaneBox.MIN_HEIGHT, PaneBox.DEFAULT_COLOR);
        ModelClass mcC = this.modelManager.createClass("C", new Point3D(400, PaneBox.INIT_DEPTH / 2, -200), PaneBox.MIN_WIDTH, PaneBox.MIN_HEIGHT, PaneBox.DEFAULT_COLOR);
        Instance iA = this.modelManager.createInstance(mcA);
        System.out.println(iA);
        
        Relation rAB = this.modelManager.createRelation(mcA, mcB, RelationType.DIRECTED_ASSOZIATION);
        System.out.println(rAB);
        
        Relation rCB = this.modelManager.createRelation(mcC, mcB, RelationType.DIRECTED_ASSOZIATION);
        System.out.println(rCB);
        
        Relation rCA = this.modelManager.createRelation(mcC, mcA, RelationType.DIRECTED_ASSOZIATION);
        System.out.println(rCA);
        
        Relation rAC = this.modelManager.createRelation(mcA, mcC, RelationType.DIRECTED_ASSOZIATION);
        System.out.println(rAC);
	}

	public void handle2DClassView() {
		SubSceneCamera ssCamera = this.subSceneAdpater.getSubSceneCamera();
		this.cameraController.handle2DClassView(ssCamera);
	}

	public void handleSetTheme(CheckMenuItem choosenMenu, Style style) {
		this.themeMenuController.handleSetTheme(this.rootLayout, choosenMenu, style);
	}

	private void initRootLayoutController() {
		FXMLLoader loader = new FXMLLoader(); // load rootlayout from fxml file
		loader.setLocation(ResourceLocator.getResourcePath(Resource.ROOTLAYOUT_FXML));
		try {
			this.rootLayout = (BorderPane) loader.load();
			addObserver(loader.getController()); // its the RootLayoutController
		} catch (IOException e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
	}

	private void initCameraController() {
		this.cameraController.handleMouse(this.subSceneAdpater);
		this.cameraController.handleKeyboard(this.subSceneAdpater);
	}

	private void initSubSceneController() {
		this.subSceneController.handleMouse(this.subSceneAdpater);
	}

	private void initPaneBoxController() {
		this.dragMoveController.addObserver(this.cameraController);
		this.dragResizeController.addObserver(this.cameraController);
		this.selectionController.addObserver(this.dragMoveController);
		this.selectionController.addObserver(this.dragResizeController);
	}

	/**
	 * Adds node to the subscene of the primary stage.
	 * 
	 * @param node
	 *            node.
	 */
	private void addToSubScene(Node node) {
		this.subSceneAdpater.add(node);
		this.rootLayout.applyCss();
	}

	/**
	 * Removes node from the subscene of the primary stage.
	 * 
	 * @param node
	 *            node.
	 */
	private void removeFromSubScene(Node node) {
		this.subSceneAdpater.remove(node);
		this.rootLayout.applyCss();
	}

	private void addRelationToSubScene(Relation relation) {
		ModelBox startModelBox = relation.getStart().getAppendant();
		ModelBox endModelBox = relation.getEnd().getAppendant();
		PaneBox startViewBox = this.boxes.get(startModelBox);
		PaneBox endViewBox = this.boxes.get(endModelBox);
		if (startViewBox != null && endViewBox != null) {
			Arrow arrow = new Arrow(startViewBox, endViewBox);
			addToSubScene(arrow);
			this.arrows.put(relation, arrow);
		}
	}

	private void adaptArrowAtBoxChanges(ModelBox modelBox) {
		PaneBox changedBox = this.boxes.get(modelBox);
		Map<Endpoint, Endpoint> endpointMap = modelBox.getFriends();
		Iterator<Endpoint> it = endpointMap.keySet().iterator();
		while(it.hasNext()) {
			Endpoint endpoint = it.next();
			Endpoint friendEndpoint = endpointMap.get(endpoint);
			PaneBox friendChangedBox = this.boxes.get(friendEndpoint.getAppendant());
			Relation relation = endpoint.getRelation();
			Arrow changedArrow = this.arrows.get(relation);
			if(changedArrow != null && changedBox != null && friendChangedBox != null) {
				if(endpoint.isStart()) {
					changedArrow.setPointsBasedOnBoxes(changedBox, friendChangedBox);
					endpoint.setCoordinates(changedArrow.getStartPoint());
					friendEndpoint.setCoordinates(changedArrow.getEndPoint());
				}
				else {
					changedArrow.setPointsBasedOnBoxes(friendChangedBox, changedBox);
					friendEndpoint.setCoordinates(changedArrow.getStartPoint());
					endpoint.setCoordinates(changedArrow.getEndPoint());
				}
				changedArrow.drawArrow();
			}
		}
	}

	private void addClassToSubScene(ModelClass theClass) {
		theClass.addObserver(this);
		PaneBox paneBox = new PaneBox();
		addPaneBoxControls(theClass, paneBox);
		addToSubScene(paneBox.get());
		addToSubScene(paneBox.getSelection());
		this.boxes.put(theClass, paneBox);
	}
	
	private void addInstanceToSubScene(Instance instance) {
		instance.addObserver(this);
		PaneBox paneBox = new PaneBox();
		paneBox.setDepth(20);
		paneBox.setColor(instance.getColor());
		//addPaneBoxControls(instance, paneBox);
		addToSubScene(paneBox.get());
		addToSubScene(paneBox.getSelection());
		this.boxes.put(instance, paneBox);
	}

	private void addPaneBoxControls(ModelClass theClass, PaneBox paneBox) {
		this.selectionController.enableSelection(paneBox, this.subSceneAdpater);
		this.textInputController.enableTextInput(theClass, paneBox);
		this.dragMoveController.enableDragMove(theClass, paneBox, this.subSceneAdpater);
		this.dragResizeController.enableDragResize(theClass, paneBox, this.subSceneAdpater);
	}

	private void adaptBoxSettings(ModelBox modelBox) {
		PaneBox changedBox = this.boxes.get(modelBox);
		if (changedBox != null) {
			changedBox.setTopText(modelBox.getName());
			changedBox.setColor(modelBox.getColor());
			changedBox.setWidth(modelBox.getWidth());
			changedBox.setHeight(modelBox.getHeight());
			changedBox.setTranslateXYZ(modelBox.getCoordinates());
		}
		adaptArrowAtBoxChanges(modelBox);
	}
	
	private void adaptInstanceBoxSettings(Instance instance) {
		//TODO
		PaneBox changedBox = this.boxes.get(instance);
		if (changedBox != null) {
			changedBox.setTopText(instance.getName());
			changedBox.setColor(instance.getColor());
			changedBox.setWidth(instance.getWidth());
			changedBox.setHeight(instance.getHeight());
			changedBox.setTranslateXYZ(instance.getCoordinates());
		}
		//adaptArrowAtClassChanges(instance);
	}

	private void adaptBoxName(ModelBox modelBox) {
		PaneBox changedBox = this.boxes.get(modelBox);
		if (changedBox != null) {
			changedBox.setTopText(modelBox.getName());
			modelBox.setWidth(changedBox.getMinWidth());
		}
	}

	private void adaptBoxWidth(ModelBox modelBox) {
		PaneBox changedBox = this.boxes.get(modelBox);
		if (changedBox != null) {
			changedBox.setWidth(modelBox.getWidth());
		}
		if(modelBox instanceof ModelClass) {
			ModelClass modelClass = (ModelClass) modelBox;
			for(Instance instance : modelClass.getInstances()) {
				instance.setWidth(modelClass.getWidth());
			}
		}
		adaptArrowAtBoxChanges(modelBox);
	}

	private void adaptBoxHeight(ModelBox modelBox) {
		PaneBox changedBox = this.boxes.get(modelBox);
		if (changedBox != null) {
			changedBox.setHeight(modelBox.getHeight());
		}
		if(modelBox instanceof ModelClass) {
			ModelClass modelClass = (ModelClass) modelBox;
			for(Instance instance : modelClass.getInstances()) {
				instance.setHeight(modelClass.getHeight());
			}
		}
		adaptArrowAtBoxChanges(modelBox);
	}

	private void adaptBoxColor(ModelBox modelBox) {
		PaneBox changedBox = this.boxes.get(modelBox);
		if (changedBox != null) {
			changedBox.setColor(modelBox.getColor());
		}
		if(modelBox instanceof ModelClass) {
			ModelClass modelClass = (ModelClass) modelBox;
			for(Instance instance : modelClass.getInstances()) {
				instance.setColor(ColorUtil.brighten(modelClass.getColor(), 5));
			}
		}
	}

	private void adaptBoxCoordinates(ModelBox modelBox) {
		PaneBox changedBox = this.boxes.get(modelBox);
		if (changedBox != null) {
			changedBox.setTranslateXYZ(modelBox.getCoordinates());
		}
		adaptArrowAtBoxChanges(modelBox);
		if(modelBox instanceof ModelClass) {
			ModelClass modelClass = (ModelClass) modelBox;
			for(Instance instance : modelClass.getInstances()) {
				instance.setX(modelClass.getX());
				instance.setZ(modelClass.getZ());
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		//TODO
		if (o instanceof ModelManager && arg instanceof ModelClass) {
			ModelClass theClass = (ModelClass) arg;
			if (!this.boxes.containsKey(theClass)) { // class is new
				addClassToSubScene(theClass);
				adaptBoxSettings(theClass);
			} else {
				PaneBox toDelete = this.boxes.remove(theClass);
				removeFromSubScene(toDelete.get());
				removeFromSubScene(toDelete.getSelection());
			}
		} else if (o instanceof ModelManager && arg instanceof Relation) {
			Relation relation = (Relation) arg;
			if (!this.arrows.containsKey(relation)) { // relation is new
				addRelationToSubScene(relation);
				// adaptRelation(relation);
			} else {
				Arrow toDelete = this.arrows.remove(relation);
				removeFromSubScene(toDelete);
			}
		} else if (o instanceof ModelManager && arg instanceof Instance) {
			Instance instance = (Instance) arg;
			if (!this.boxes.containsKey(instance)) { // instance is new			
				addInstanceToSubScene(instance);
				adaptInstanceBoxSettings(instance);
			} else {
				PaneBox toDelete = this.boxes.remove(instance);
				removeFromSubScene(toDelete.get());
				removeFromSubScene(toDelete.getSelection());
			}
		} else if (o instanceof ModelBox && arg instanceof ModelBoxChange) {
			ModelBox modelBox = (ModelBox) o;
			ModelBoxChange modelBoxChange = (ModelBoxChange) arg;
			switch (modelBoxChange) {
			case COLOR:
				adaptBoxColor(modelBox);
				break;
			case COORDINATES:
				adaptBoxCoordinates(modelBox);
				break;
			case HEIGHT:
				adaptBoxHeight(modelBox);
				break;
			case NAME:
				adaptBoxName(modelBox);
				break;
			case WIDTH:
				adaptBoxWidth(modelBox);
				break;
			default:
				break;
			}
		}
	}

}
