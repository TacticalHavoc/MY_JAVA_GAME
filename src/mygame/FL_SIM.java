package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.control.RigidBodyControl;
import static com.jme3.math.FastMath.PI;
import com.jme3.math.Vector2f;



public class FL_SIM extends SimpleApplication {
    private Node mignode;
    private Spatial mig;
    private float moveSpeed = 500.0f;
    private CameraNode camNode;

    private Quaternion originalRotation;
    private boolean isTiltingLeft = false;
    private boolean isTiltingRight = false;
    private boolean isMovingForward = false;
    private boolean isMovingUp = false;
    private boolean isMovingDown = false;
    private float tiltSpeed = 3f;

    // Reusing Quaternion instances to avoid memory allocation each frame
    private Quaternion tiltLeft = new Quaternion();
    private Quaternion tiltRight = new Quaternion();
    private Quaternion moveUp = new Quaternion();
    private Quaternion moveDown = new Quaternion();

    private Texture grass, dirt, rock;

    public static void main(String[] args) {
        FL_SIM app = new FL_SIM();
        AppSettings settings = new AppSettings(true);
        settings.setResizable(true);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
       
  
        grass = assetManager.loadTexture("Textures/GRASS.jpg");
        dirt = assetManager.loadTexture("Textures/GROUND.jpg");
        rock = assetManager.loadTexture("Textures/GROUND.jpg");

        // Initialize the model and node
        mignode = new Node("MIG NODE");
        mig = assetManager.loadModel("FELON/scene.j3o");
        mig.setLocalScale(0.023f);
        mig.setLocalTranslation(0, 0, 0);
        mignode.setLocalTranslation(0, 0, 0);
        mignode.attachChild(mig);
        rootNode.attachChild(mignode);

        originalRotation = mignode.getLocalRotation().clone();

 
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
        rootNode.addLight(sun);

        createTerrain();
        addSky();

   
        camNode = new CameraNode("Camera Node", cam);
        camNode.setLocalTranslation(0, 6, -9);  
        rootNode.attachChild(camNode);

        // Setup input keys
        setupKeys();
        inputManager.setCursorVisible(true);
    }

    private void setupKeys() {
        inputManager.addMapping("move forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("move down", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("move up", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("tilt left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("tilt right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addListener(actionListener, "move forward", "move down", "move up", "tilt left", "tilt right");
    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("tilt left")) {
                isTiltingLeft = isPressed;
            } else if (name.equals("tilt right")) {
                isTiltingRight = isPressed;
            } else if (name.equals("move forward")) {
                isMovingForward = isPressed;
            } else if (name.equals("move down")) {
                isMovingDown = isPressed;
            } else if (name.equals("move up")) {
                isMovingUp = isPressed;
            }
        }
    };

    private void createTerrain() {
        Texture heightMapImage = assetManager.loadTexture("MOUNTA/MOUNTAINS.jpg");
        AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.5f);
        heightmap.load();

        TerrainQuad terrain = new TerrainQuad("myTerrain", 65, 4097, heightmap.getHeightMap());

        Material mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/GROUND.jpg"));

        mat_terrain.setTexture("Tex1", grass);
        mat_terrain.setFloat("Tex1Scale", 64f);

        mat_terrain.setTexture("Tex2", dirt);
        mat_terrain.setFloat("Tex2Scale", 32f);

        mat_terrain.setTexture("Tex3", rock);
        mat_terrain.setFloat("Tex3Scale", 128f);

        terrain.setMaterial(mat_terrain);
        terrain.setLocalTranslation(0, -100, 0);
        terrain.setLocalScale(2f, 1f, 2f);

        rootNode.attachChild(terrain);
    }

    private void addSky() {
        TextureKey skyTextureKey = new TextureKey("Textures/Sky.jpg", true);
        Texture skyTexture = assetManager.loadTexture(skyTextureKey);
        Spatial sky = SkyFactory.createSky(assetManager, skyTexture, SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);
    }

    @Override
    public void simpleUpdate(float tpf) {
        int j =0;
    inputManager.setCursorVisible(true);
       
        if (isMovingForward) {
           
            Vector3f forwardDir = mignode.getLocalRotation().mult(Vector3f.UNIT_Z);
            mignode.move(forwardDir.mult(moveSpeed * tpf));

            Vector3f camForwardDir = camNode.getLocalRotation().mult(Vector3f.UNIT_Z);
            camNode.move(camForwardDir.mult(moveSpeed * tpf));
        }

        if (isMovingDown) {
            moveDown.fromAngleAxis(FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_X);
            mignode.rotate(moveDown);
            camNode.rotate(moveDown);
        }

        if (isMovingUp) {
            moveUp.fromAngleAxis(-FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_X);
            mignode.rotate(moveUp);
            camNode.rotate(moveUp);
        }

        if (isTiltingLeft) {
            tiltLeft.fromAngleAxis(-FastMath.PI / 2 * tpf * 1.5f, new Vector3f(0, 0, 1));
            mignode.rotate(tiltLeft);
            camNode.rotate(tiltLeft);

            Vector3f currentPos = mignode.getLocalTranslation();
            mignode.setLocalTranslation(currentPos.add(moveSpeed * tpf / 15, 0, 0));

            Vector3f camCurrentPos = camNode.getLocalTranslation();
            camNode.setLocalTranslation(camCurrentPos.add(moveSpeed * tpf / 15, 0, 0));
        }

        if (isTiltingRight) {
            tiltRight.fromAngleAxis(FastMath.PI / 2 * tpf * 1.5f, new Vector3f(0, 0, 1));
            mignode.rotate(tiltRight);
            camNode.rotate(tiltRight);

            Vector3f currentPos = mignode.getLocalTranslation();
            mignode.setLocalTranslation(currentPos.add(-moveSpeed * tpf / 15, 0, 0));

            Vector3f camCurrentPos = camNode.getLocalTranslation();
            camNode.setLocalTranslation(camCurrentPos.add(-moveSpeed * tpf / 15, 0, 0));
        }

        camNode.lookAt(mignode.getLocalTranslation().add(0, 2, 0), Vector3f.UNIT_Y);  
    }

    @Override
    public void simpleRender(RenderManager rm) {
    
    }
}




















//public class FL_SIM extends SimpleApplication {
//    private Node mignode;
//    private Spatial mig;
//    private float moveSpeed = 500.0f;
//    private CameraNode camNode;
//
//    private Quaternion originalRotation;
//    private boolean isTiltingLeft = false;
//    private boolean isTiltingRight = false;
//    private boolean isMovingForward = false;
//    private boolean isMovingUp = false;
//    private boolean isMovingDown = false;
//    private float tiltSpeed = 3f;
//
//    public static void main(String[] args) {
//        FL_SIM app = new FL_SIM();
//        AppSettings settings = new AppSettings(true);
//        settings.setResizable(true);
//        app.setSettings(settings);
//        app.start();
//    }
//
//    @Override
//    public void simpleInitApp() {
//        mignode = new Node("MIG NODE");
//        mig = assetManager.loadModel("FELON/scene.j3o");
//        mig.setLocalScale(0.023f);
//        mig.setLocalTranslation(0, 0, 0);
//        mignode.setLocalTranslation(0, 0, 0);
//        mignode.attachChild(mig);
//        rootNode.attachChild(mignode);
//
//        originalRotation = mignode.getLocalRotation().clone();
//
//        DirectionalLight sun = new DirectionalLight();
//        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
//        rootNode.addLight(sun);
//
//        createTerrain();
//        addSky();
//
//        camNode = new CameraNode("Camera Node", cam);
//        rootNode.attachChild(camNode);
//
//        setupKeys();
//        inputManager.setCursorVisible(true);
//    }
//
//    private void setupKeys() {
//        inputManager.addMapping("move forward", new KeyTrigger(KeyInput.KEY_W));
//        inputManager.addMapping("move down", new KeyTrigger(KeyInput.KEY_DOWN));
//        inputManager.addMapping("move up", new KeyTrigger(KeyInput.KEY_UP));
//        inputManager.addMapping("tilt left", new KeyTrigger(KeyInput.KEY_A));
//        inputManager.addMapping("tilt right", new KeyTrigger(KeyInput.KEY_D));
//        inputManager.addListener(actionListener, "move forward", "move down", "move up", "tilt left", "tilt right");
//    }
//
//    private ActionListener actionListener = new ActionListener() {
//        @Override
//        public void onAction(String name, boolean isPressed, float tpf) {
//            if (name.equals("tilt left")) {
//                isTiltingLeft = isPressed;
//            } else if (name.equals("tilt right")) {
//                isTiltingRight = isPressed;
//            } else if (name.equals("move forward")) {
//                isMovingForward = isPressed;
//            } else if (name.equals("move down")) {
//                isMovingDown = isPressed;
//            } else if (name.equals("move up")) {
//                isMovingUp = isPressed;
//            }
//        }
//    };
//
//    private void createTerrain() {
//        Texture heightMapImage = assetManager.loadTexture("MOUNTA/MOUNTAINS.jpg");
//        AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.5f);
//        heightmap.load();
//
//        TerrainQuad terrain = new TerrainQuad("myTerrain", 65, 4097, heightmap.getHeightMap());
//
//        Material mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
//        mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/GROUND.jpg"));
//
//        Texture grass = assetManager.loadTexture("Textures/GRASS.jpg");
//        grass.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex1", grass);
//        mat_terrain.setFloat("Tex1Scale", 64f);
//
//        Texture dirt = assetManager.loadTexture("Textures/GROUND.jpg");
//        dirt.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex2", dirt);
//        mat_terrain.setFloat("Tex2Scale", 32f);
//
//        Texture rock = assetManager.loadTexture("Textures/GROUND.jpg");
//        rock.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex3", rock);
//        mat_terrain.setFloat("Tex3Scale", 128f);
//
//        terrain.setMaterial(mat_terrain);
//        terrain.setLocalTranslation(0, -100, 0);
//        terrain.setLocalScale(2f, 1f, 2f);
//
//        rootNode.attachChild(terrain);
//    }
//
//    private void addSky() {
//        TextureKey skyTextureKey = new TextureKey("Textures/Sky.jpg", true);
//        Texture skyTexture = assetManager.loadTexture(skyTextureKey);
//        Spatial sky = SkyFactory.createSky(assetManager, skyTexture, SkyFactory.EnvMapType.EquirectMap);
//        rootNode.attachChild(sky);
//    }
//
//    @Override
//    public void simpleUpdate(float tpf) {
//        inputManager.setCursorVisible(true);
//
//        if (isMovingForward) {
//            Vector3f forwardDir = mignode.getLocalRotation().mult(Vector3f.UNIT_Z);
//            mignode.move(forwardDir.mult(moveSpeed * tpf));
//        }
//
//        if (isMovingDown) {
//            Quaternion facedown = new Quaternion();
//            facedown.fromAngleAxis(FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_X);
//            mignode.rotate(facedown);
//        }
//
//        if (isMovingUp) {
//            Quaternion faceup = new Quaternion();
//            faceup.fromAngleAxis(-FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_X);
//            mignode.rotate(faceup);
//        }
//
//        if (isTiltingLeft) {
//            Quaternion tilt = new Quaternion();
//            tilt.fromAngleAxis(-FastMath.PI / 2 * tpf * 1.5f, new Vector3f(0, 0, 1));
//            mignode.rotate(tilt);
//            Vector3f currentPos = mignode.getLocalTranslation();
//            mignode.setLocalTranslation(currentPos.add(moveSpeed * tpf / 15, 0, 0));
//        }
//
//        if (isTiltingRight) {
//            Quaternion tilt = new Quaternion();
//            tilt.fromAngleAxis(FastMath.PI / 2 * tpf * 1.5f, new Vector3f(0, 0, 1));
//            mignode.rotate(tilt);
//            Vector3f currentPos = mignode.getLocalTranslation();
//            mignode.setLocalTranslation(currentPos.add(-moveSpeed * tpf / 15, 0, 0));
//        }
//
//        // Camera follows the plane at a fixed offset and looks at the plane's direction
//        Vector3f camOffset = new Vector3f(0, 2, -10);
//        Vector3f targetCamPos = mignode.getLocalTranslation().add(camOffset);
//        camNode.setLocalTranslation(targetCamPos);
//
//        camNode.lookAt(mignode.getLocalTranslation(), Vector3f.UNIT_Y);
//       
//    }
//
//    @Override
//    public void simpleRender(RenderManager rm) {
//    }
//}

//public class FL_SIM extends SimpleApplication {
//    private Node mignode;
//    private Spatial mig;
//    private float moveSpeed = 500.0f;
//    private CameraNode camNode;
//
//    private Quaternion originalRotation;
//    private boolean isTiltingLeft = false;
//    private boolean isTiltingRight = false;
//    private boolean isMovingForward = false;
//    private boolean isMovingUp = false;
//    private boolean isMovingDown = false;
//    private float tiltSpeed = 3f;
//
//    public static void main(String[] args) {
//        FL_SIM app = new FL_SIM();
//        AppSettings settings = new AppSettings(true);
//        settings.setResizable(true);
//        app.setSettings(settings);
//        app.start();
//    }
//
//    @Override
//    public void simpleInitApp() {
//        mignode = new Node("MIG NODE");
//        mig = assetManager.loadModel("FELON/scene.j3o");
//        mig.setLocalScale(0.023f);
//        mig.setLocalTranslation(0, 0, 0);
//        mignode.setLocalTranslation(0, 0, 0);
//        mignode.attachChild(mig);
//        rootNode.attachChild(mignode);
//
//        originalRotation = mignode.getLocalRotation().clone();
//
//        DirectionalLight sun = new DirectionalLight();
//        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
//        rootNode.addLight(sun);
//
//        createTerrain();
//        addSky();
//
//        camNode = new CameraNode("Camera Node", cam);
//     
//        camNode.setLocalTranslation(0, 4, -11);
//       rootNode.attachChild(camNode);
//        setupKeys();
//        inputManager.setCursorVisible(true);
//    }
//
//    private void setupKeys() {
//        inputManager.addMapping("move forward", new KeyTrigger(KeyInput.KEY_W));
//        inputManager.addMapping("move down", new KeyTrigger(KeyInput.KEY_DOWN));
//        inputManager.addMapping("move up", new KeyTrigger(KeyInput.KEY_UP));
//        inputManager.addMapping("tilt left", new KeyTrigger(KeyInput.KEY_A));
//        inputManager.addMapping("tilt right", new KeyTrigger(KeyInput.KEY_D));
//        inputManager.addListener(actionListener, "move forward", "move down", "move up", "tilt left", "tilt right");
//    }
//
//    private ActionListener actionListener = new ActionListener() {
//        @Override
//        public void onAction(String name, boolean isPressed, float tpf) {
//            if (name.equals("tilt left")) {
//                isTiltingLeft = isPressed;
//            } else if (name.equals("tilt right")) {
//                isTiltingRight = isPressed;
//            } else if (name.equals("move forward")) {
//                isMovingForward = isPressed;
//            } else if (name.equals("move down")) {
//                isMovingDown = isPressed;
//            } else if (name.equals("move up")) {
//                isMovingUp = isPressed;
//            }
//        }
//    };
//
//    private void createTerrain() {
//        Texture heightMapImage = assetManager.loadTexture("MOUNTA/MOUNTAINS.jpg");
//        AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.5f);
//        heightmap.load();
//
//        TerrainQuad terrain = new TerrainQuad("myTerrain", 65, 4097, heightmap.getHeightMap());
//
//        Material mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
//        mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/GROUND.jpg"));
//
//        Texture grass = assetManager.loadTexture("Textures/GRASS.jpg");
//        grass.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex1", grass);
//        mat_terrain.setFloat("Tex1Scale", 64f);
//
//        Texture dirt = assetManager.loadTexture("Textures/GROUND.jpg");
//        dirt.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex2", dirt);
//        mat_terrain.setFloat("Tex2Scale", 32f);
//
//        Texture rock = assetManager.loadTexture("Textures/GROUND.jpg");
//        rock.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex3", rock);
//        mat_terrain.setFloat("Tex3Scale", 128f);
//
//        terrain.setMaterial(mat_terrain);
//        terrain.setLocalTranslation(0, -100, 0);
//        terrain.setLocalScale(2f, 1f, 2f);
//
//        rootNode.attachChild(terrain);
//    }
//
//    private void addSky() {
//        TextureKey skyTextureKey = new TextureKey("Textures/Sky.jpg", true);
//        Texture skyTexture = assetManager.loadTexture(skyTextureKey);
//        Spatial sky = SkyFactory.createSky(assetManager, skyTexture, SkyFactory.EnvMapType.EquirectMap);
//        rootNode.attachChild(sky);
//    }
//
//    @Override
//    public void simpleUpdate(float tpf) {
//        inputManager.setCursorVisible(true);
//
//        if (isMovingForward) {
//            Vector3f forwardDir = mignode.getLocalRotation().mult(Vector3f.UNIT_Z);
//            mignode.move(forwardDir.mult(moveSpeed * tpf));
//            
//            Vector3f camforwardDir = camNode.getLocalRotation().mult(Vector3f.UNIT_Z);
//            camNode.move(camforwardDir.mult(moveSpeed * tpf));
//             
//        }
//
//        if (isMovingDown) {
//            Quaternion facedown = new Quaternion();
//            facedown.fromAngleAxis(FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_X);
//            mignode.rotate(facedown);
//
//            Quaternion camfacedown = new Quaternion();
//            camfacedown.fromAngleAxis(FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_X);
//            camNode.rotate(camfacedown);
//        }
//
//        if (isMovingUp) {
//            Quaternion faceup = new Quaternion();
//            faceup.fromAngleAxis(-FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_X);
//            mignode.rotate(faceup);
//
//            Quaternion camfaceup = new Quaternion();
//            camfaceup.fromAngleAxis(-FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_X);
//            camNode.rotate(camfaceup);
//        }
//
//        if (isTiltingLeft) {
//            Quaternion tilt = new Quaternion();
//            tilt.fromAngleAxis(-FastMath.PI / 2 * tpf * 1.5f, new Vector3f(0, 0, 1));
//            mignode.rotate(tilt);
//            camNode.rotate(tilt);
//            Vector3f currentPos = mignode.getLocalTranslation();
//            mignode.setLocalTranslation(currentPos.add(moveSpeed * tpf / 15, 0, 0));
//            Vector3f camcurrentPos = camNode.getLocalTranslation();
//            camNode.setLocalTranslation(camcurrentPos.add(moveSpeed * tpf / 15, 0, 0));
//        }
//
//        if (isTiltingRight) {
//            Quaternion tilt = new Quaternion();
//            tilt.fromAngleAxis(FastMath.PI / 2 * tpf * 1.5f, new Vector3f(0, 0, 1));
//            mignode.rotate(tilt);
//            camNode.rotate(tilt);
//            Vector3f currentPos = mignode.getLocalTranslation();
//            mignode.setLocalTranslation(currentPos.add(-moveSpeed * tpf / 15, 0, 0));
//            Vector3f camcurrentPos = camNode.getLocalTranslation();
//            camNode.setLocalTranslation(camcurrentPos.add(-moveSpeed * tpf / 15, 0, 0));
//        }
//
//        camNode.lookAt(mignode.getLocalTranslation(), Vector3f.UNIT_Y);
//    }
//
//    @Override
//    public void simpleRender(RenderManager rm) {
//    }
//}



//
//
//public class FL_SIM extends SimpleApplication {
//    private Node mignode;
//    private Spatial mig;
//    private float moveSpeed = 500.0f;
//    private CameraNode camNode;
//
//    private Quaternion originalRotation;
//    private boolean isTiltingLeft = false;
//    private boolean isTiltingRight = false;
//    private boolean isMovingForward = false;
//    private boolean isMovingUp = false;
//    private boolean isMovingDown = false;
//    private float tiltSpeed = 3f;
//
//    public static void main(String[] args) {
//        FL_SIM app = new FL_SIM();
//        AppSettings settings = new AppSettings(true);
//        settings.setResizable(true);
//        app.setSettings(settings);
//        app.start();
//    }
//
//    @Override
//    public void simpleInitApp() {
//        mignode = new Node("MIG NODE");
//        mig = assetManager.loadModel("FELON/scene.j3o");
//        mig.setLocalScale(0.023f);
//        mig.setLocalTranslation(0,0,0);
//        mignode.setLocalTranslation(0,0,0);
//        mignode.attachChild(mig);
//        rootNode.attachChild(mignode);
//
//        originalRotation = mignode.getLocalRotation().clone();
//
//        DirectionalLight sun = new DirectionalLight();
//        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
//        rootNode.addLight(sun);
//
//        createTerrain();
//        addSky();
//
//        camNode = new CameraNode("Camera Node", cam);
//       
//        //camNode.setControlDir(ControlDirection.SpatialToCamera);
//        
////        camNode.setLocalTranslation(new Vector3f(0, 6, -14));
////        camNode.lookAt(mignode.getLocalTranslation(), Vector3f.UNIT_Y);
//        rootNode.attachChild(camNode); // Attach camera node to root node instead of mignode
//
//        setupKeys();
//        inputManager.setCursorVisible(true);
//    }
//
//    private void setupKeys() {
//        inputManager.addMapping("move forward", new KeyTrigger(KeyInput.KEY_W));
//        inputManager.addMapping("move down", new KeyTrigger(KeyInput.KEY_DOWN));
//        inputManager.addMapping("move up", new KeyTrigger(KeyInput.KEY_UP));
//        inputManager.addMapping("tilt left", new KeyTrigger(KeyInput.KEY_A));
//        inputManager.addMapping("tilt right", new KeyTrigger(KeyInput.KEY_D));
//        inputManager.addListener(actionListener, "move forward", "move down", "move up", "tilt left", "tilt right");
//    }
//
//    private ActionListener actionListener = new ActionListener() {
//        @Override
//        public void onAction(String name, boolean isPressed, float tpf) {
//            if (name.equals("tilt left")) {
//                isTiltingLeft = isPressed;
//            } else if (name.equals("tilt right")) {
//                isTiltingRight = isPressed;
//            } else if (name.equals("move forward")) {
//                isMovingForward = isPressed;
//            } else if (name.equals("move down")) {
//                isMovingDown = isPressed;
//            } else if (name.equals("move up")) {
//                isMovingUp = isPressed;
//            }
//        }
//    };
//
//    private void createTerrain() {
//        Texture heightMapImage = assetManager.loadTexture("MOUNTA/MOUNTAINS.jpg");
//        AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.5f);
//        heightmap.load();
//
//        TerrainQuad terrain = new TerrainQuad("myTerrain", 65, 4097, heightmap.getHeightMap());
//
//        Material mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
//        mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/GROUND.jpg"));
//
//        Texture grass = assetManager.loadTexture("Textures/GRASS.jpg");
//        grass.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex1", grass);
//        mat_terrain.setFloat("Tex1Scale", 64f);
//
//        Texture dirt = assetManager.loadTexture("Textures/GROUND.jpg");
//        dirt.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex2", dirt);
//        mat_terrain.setFloat("Tex2Scale", 32f);
//
//        Texture rock = assetManager.loadTexture("Textures/GROUND.jpg");
//        rock.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex3", rock);
//        mat_terrain.setFloat("Tex3Scale", 128f);
//
//        terrain.setMaterial(mat_terrain);
//        terrain.setLocalTranslation(0, -100, 0);
//        terrain.setLocalScale(2f, 1f, 2f);
//
//        rootNode.attachChild(terrain);
//    }
//
//    private void addSky() {
//        TextureKey skyTextureKey = new TextureKey("Textures/Sky.jpg", true);
//        Texture skyTexture = assetManager.loadTexture(skyTextureKey);
//        Spatial sky = SkyFactory.createSky(assetManager, skyTexture, SkyFactory.EnvMapType.EquirectMap);
//        rootNode.attachChild(sky);
//    }
//
//    @Override
//    public void simpleUpdate(float tpf) {
//        
//        inputManager.setCursorVisible(true);
//        
//   
//         
//         
//        if (isMovingForward) {
//            Vector3f currentPos = mignode.getLocalTranslation();
//            mignode.setLocalTranslation(currentPos.add(0, 0, moveSpeed * tpf));
//        }
//
//        if (isMovingUp) {
//            Quaternion tilty = new Quaternion();
//            tilty.fromAngleAxis(-FastMath.PI / 6 * tpf, new Vector3f(1, 0, 0));
//            mignode.rotate(tilty);
//            Vector3f currentPos = mignode.getLocalTranslation();
//            mignode.setLocalTranslation(currentPos.add(0, moveSpeed * tpf, 0));
//        }
//
//        if (isMovingDown) {
//              Quaternion tilty = new Quaternion();
//            tilty.fromAngleAxis(-FastMath.PI / 6 * tpf, new Vector3f(-1, 0, 0));
//            mignode.rotate(tilty);
//            Vector3f currentPos = mignode.getLocalTranslation();
//            mignode.setLocalTranslation(currentPos.add(0, -moveSpeed * tpf, 0));
//            
//        }
//
//
//        if (isTiltingLeft) {
//            Quaternion tilt = new Quaternion();
//            tilt.fromAngleAxis(-FastMath.PI / 2 * tpf*1.5f, new Vector3f(0, 0, 1));
//            mignode.rotate(tilt);
//            Vector3f currentPos = mignode.getLocalTranslation();
//            mignode.setLocalTranslation(currentPos.add(moveSpeed * tpf/15, 0, 0));
//        }
//
//        if (isTiltingRight) {
//            Quaternion tilt = new Quaternion();
//            tilt.fromAngleAxis(FastMath.PI / 2 * tpf*1.5f, new Vector3f(0, 0, 1));
//            mignode.rotate(tilt);
//            Vector3f currentPos = mignode.getLocalTranslation();
//            mignode.setLocalTranslation(currentPos.add(-moveSpeed * tpf/15, 0, 0));
//        }
//
//        if (!isTiltingLeft && !isTiltingRight && !isMovingUp && !isMovingDown && !mignode.getLocalRotation().equals(originalRotation)) {
//            Quaternion currentRotation = mignode.getLocalRotation();
//            Quaternion interpolatedRotation = new Quaternion();
//            interpolatedRotation.slerp(currentRotation, originalRotation, tiltSpeed * tpf);
//            mignode.setLocalRotation(interpolatedRotation);
//        }
//
//        // Update camera position and rotation to follow the model
//        camNode.setLocalTranslation(mignode.getLocalTranslation().add(0, 1, -5));
//      camNode.setLocalRotation(mig.getLocalRotation());
//
//
//       // camNode.lookAt(mignode.getLocalTranslation(), Vector3f.UNIT_Y);
//
//    }
//
//    @Override
//    public void simpleRender(RenderManager rm) {}
//}


//public class FL_SIM extends SimpleApplication {
//    private Node mignode;
//    private Spatial mig;
//    private float moveSpeed = 500.0f;
//    private CameraNode camNode;
//
//    private Quaternion originalRotation;
//    private boolean isTiltingLeft = false;
//    private boolean isTiltingRight = false;
//    private boolean isMovingForward = false;
//    private boolean isMovingUp = false;
//    private boolean isMovingDown = false;
//    private boolean isYawingLeft = false;  // Added
//    private boolean isYawingRight = false; // Added
//    private float tiltSpeed = 3f;
//
//    public static void main(String[] args) {
//        FL_SIM app = new FL_SIM();
//        AppSettings settings = new AppSettings(true);
//        settings.setResizable(true);
//        app.setSettings(settings);
//        app.start();
//    }
//
//    @Override
//    public void simpleInitApp() {
//        mignode = new Node("MIG NODE");
//        mig = assetManager.loadModel("FELON/scene.j3o");
//        mig.setLocalScale(0.023f);  // This will scale the model to half its size. 
//        mig.setLocalTranslation(0,0,0);
//        mignode.setLocalTranslation(0,0,0);
//        mignode.attachChild(mig);
//        rootNode.attachChild(mignode);
//
//        originalRotation = mignode.getLocalRotation().clone();
//
//        DirectionalLight sun = new DirectionalLight();
//        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
//        rootNode.addLight(sun);
//
//        createTerrain();
//        addSky();
//
//        camNode = new CameraNode("Camera Node", cam);
//        rootNode.attachChild(camNode);
//
//        setupKeys();
//        inputManager.setCursorVisible(true);
//    }
//
//    private void setupKeys() {
//        inputManager.addMapping("move forward", new KeyTrigger(KeyInput.KEY_W));
//        inputManager.addMapping("move backward", new KeyTrigger(KeyInput.KEY_S));
//        inputManager.addMapping("move up", new KeyTrigger(KeyInput.KEY_E));  // Changed
//        inputManager.addMapping("move down", new KeyTrigger(KeyInput.KEY_Q)); // Changed
//        inputManager.addMapping("tilt left", new KeyTrigger(KeyInput.KEY_A));
//        inputManager.addMapping("tilt right", new KeyTrigger(KeyInput.KEY_D));
//        inputManager.addMapping("yaw left", new KeyTrigger(KeyInput.KEY_DOWN));  // Added
//        inputManager.addMapping("yaw right", new KeyTrigger(KeyInput.KEY_UP)); // Added
//        inputManager.addListener(actionListener, "move forward", "move backward", "move up", "move down", "tilt left", "tilt right", "yaw left", "yaw right");
//    }
//
//    private ActionListener actionListener = new ActionListener() {
//        @Override
//        public void onAction(String name, boolean isPressed, float tpf) {
//            switch (name) {
//                case "move forward":
//                    isMovingForward = isPressed;
//                    break;
//                case "move backward":
//                    isMovingDown = isPressed;  // Adjusted for simplicity
//                    break;
//                case "move up":
//                    isMovingUp = isPressed;
//                    break;
//                case "move down":
//                    isMovingDown = isPressed;
//                    break;
//                case "tilt left":
//                    isTiltingLeft = isPressed;
//                    break;
//                case "tilt right":
//                    isTiltingRight = isPressed;
//                    break;
//                case "yaw left":
//                    isYawingLeft = isPressed;
//                    break;
//                case "yaw right":
//                    isYawingRight = isPressed;
//                    break;
//            }
//        }
//    };
//
//    private void createTerrain() {
//        Texture heightMapImage = assetManager.loadTexture("MOUNTA/MOUNTAINS.jpg");
//        AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.5f);
//        heightmap.load();
//
//        TerrainQuad terrain = new TerrainQuad("myTerrain", 65, 4097, heightmap.getHeightMap());
//
//        Material mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
//        mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/GROUND.jpg"));
//
//        Texture grass = assetManager.loadTexture("Textures/GRASS.jpg");
//        grass.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex1", grass);
//        mat_terrain.setFloat("Tex1Scale", 64f);
//
//        Texture dirt = assetManager.loadTexture("Textures/GROUND.jpg");
//        dirt.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex2", dirt);
//        mat_terrain.setFloat("Tex2Scale", 32f);
//
//        Texture rock = assetManager.loadTexture("Textures/GROUND.jpg");
//        rock.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex3", rock);
//        mat_terrain.setFloat("Tex3Scale", 128f);
//
//        terrain.setMaterial(mat_terrain);
//        terrain.setLocalTranslation(0, -100, 0);
//        terrain.setLocalScale(2f, 1f, 2f);
//
//        rootNode.attachChild(terrain);
//    }
//
//    private void addSky() {
//        TextureKey skyTextureKey = new TextureKey("Textures/Sky.jpg", true);
//        Texture skyTexture = assetManager.loadTexture(skyTextureKey);
//        Spatial sky = SkyFactory.createSky(assetManager, skyTexture, SkyFactory.EnvMapType.EquirectMap);
//        rootNode.attachChild(sky);
//    }
//
//    @Override
//    public void simpleUpdate(float tpf) {
//        inputManager.setCursorVisible(true);
//        
//        // Move forward in the direction the nose is pointing
//        if (isMovingForward) {
//            Vector3f forwardDir = mignode.getLocalRotation().mult(Vector3f.UNIT_Z);
//            mignode.move(forwardDir.mult(moveSpeed * tpf));
//        }
//
//        // Move backward in the opposite direction the nose is pointing
//        if (isMovingDown) {  // Adjusted for simplicity
//            Vector3f backwardDir = mignode.getLocalRotation().mult(Vector3f.UNIT_Z).negate();
//            mignode.move(backwardDir.mult(moveSpeed * tpf));
//        }
//
//        // Move up along the plane's local Y axis
//        if (isMovingUp) {
//            Vector3f upDir = mignode.getLocalRotation().mult(Vector3f.UNIT_Y);
//            mignode.move(upDir.mult(moveSpeed * tpf));
//        }
//
//        // Move down along the plane's local Y axis
//        if (isMovingDown) {
//            Vector3f downDir = mignode.getLocalRotation().mult(Vector3f.UNIT_Y).negate();
//            mignode.move(downDir.mult(moveSpeed * tpf));
//        }
//
//        // Tilt left along the plane's local Z axis
//        if (isTiltingLeft) {
//            Quaternion tilt = new Quaternion();
//            tilt.fromAngleAxis(-FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_Z);
//            mignode.rotate(tilt);
//        }
//
//        // Tilt right along the plane's local Z axis
//        if (isTiltingRight) {
//            Quaternion tilt = new Quaternion();
//            tilt.fromAngleAxis(FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_Z);
//            mignode.rotate(tilt);
//        }
//
//        // Yaw left along the plane's local Y axis
//        if (isYawingLeft) {
//            Quaternion yaw = new Quaternion();
//            yaw.fromAngleAxis(FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_X);
//            mignode.rotate(yaw);
//        }
//
//        // Yaw right along the plane's local Y axis
//        if (isYawingRight) {
//            Quaternion yaw = new Quaternion();
//            yaw.fromAngleAxis(-FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_X);
//            mignode.rotate(yaw);
//        }
//
//        // Gradually return to the original rotation if no inputs are detected
//        if (!isTiltingLeft && !isTiltingRight && !isYawingLeft && !isYawingRight && !isMovingUp && !isMovingDown && !isMovingForward && !mignode.getLocalRotation().equals(originalRotation)) {
//            Quaternion currentRotation = mignode.getLocalRotation();
//            Quaternion interpolatedRotation = new Quaternion();
//            interpolatedRotation.slerp(currentRotation, originalRotation, tiltSpeed * tpf);
//            mignode.setLocalRotation(interpolatedRotation);
//        }
//
//        // Update camera position and rotation to follow the model
//      
//  Vector3f camOffset = new Vector3f(0, 1.5f, -5); 
//  Vector3f camPosition; 
//   
//       camPosition = mignode.getLocalRotation().mult(camOffset).add(mignode.getLocalTranslation());
//  camNode.setLocalTranslation(camPosition); 
////  camNode.setLocalRotation(mignode.getLocalRotation());
//
//
//    }
//}

//--------------------------------------------------------------------------------------------------------------------------


//public class FL_SIM extends SimpleApplication {
//    private Node mignode;
//    private Spatial mig;
//    private float moveSpeed = 500.0f;
//    private CameraNode camNode;
//
//    private Quaternion originalRotation;
//    private boolean isTiltingLeft = false;
//    private boolean isTiltingRight = false;
//    private boolean isMovingForward = false;
//    private boolean isMovingUp = false;
//    private boolean isMovingDown = false;
//    private float tiltSpeed = 3f;
//
//    public static void main(String[] args) {
//        FL_SIM app = new FL_SIM();
//        AppSettings settings = new AppSettings(true);
//        settings.setResizable(true);
//        app.setSettings(settings);
//        app.start();
//    }
//
//    @Override
//    public void simpleInitApp() {
//        mignode = new Node("MIG NODE");
//        mig = assetManager.loadModel("FELON/scene.j3o");
//        mig.setLocalScale(0.023f);
//        mig.setLocalTranslation(0, 0, 0);
//        mignode.setLocalTranslation(0, 0, 0);
//        mignode.attachChild(mig);
//        rootNode.attachChild(mignode);
//
//        originalRotation = mignode.getLocalRotation().clone();
//
//        DirectionalLight sun = new DirectionalLight();
//        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
//        rootNode.addLight(sun);
//
//        createTerrain();
//        addSky();
//
//        camNode = new CameraNode("Camera Node", cam);
//        rootNode.attachChild(camNode); // Attach camera node to root node instead of mignode
//
//        setupKeys();
//        inputManager.setCursorVisible(true);
//    }
//
//    private void setupKeys() {
//        inputManager.addMapping("move forward", new KeyTrigger(KeyInput.KEY_W));
//        inputManager.addMapping("move down", new KeyTrigger(KeyInput.KEY_DOWN));
//        inputManager.addMapping("move up", new KeyTrigger(KeyInput.KEY_UP));
//        inputManager.addMapping("tilt left", new KeyTrigger(KeyInput.KEY_A));
//        inputManager.addMapping("tilt right", new KeyTrigger(KeyInput.KEY_D));
//        inputManager.addListener(actionListener, "move forward", "move down", "move up", "tilt left", "tilt right");
//    }
//
//    private ActionListener actionListener = new ActionListener() {
//        @Override
//        public void onAction(String name, boolean isPressed, float tpf) {
//            if (name.equals("tilt left")) {
//                isTiltingLeft = isPressed;
//            } else if (name.equals("tilt right")) {
//                isTiltingRight = isPressed;
//            } else if (name.equals("move forward")) {
//                isMovingForward = isPressed;
//            } else if (name.equals("move down")) {
//                isMovingDown = isPressed;
//            } else if (name.equals("move up")) {
//                isMovingUp = isPressed;
//            }
//        }
//    };
//
//    private void createTerrain() {
//        Texture heightMapImage = assetManager.loadTexture("MOUNTA/MOUNTAINS.jpg");
//        AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.5f);
//        heightmap.load();
//
//        TerrainQuad terrain = new TerrainQuad("myTerrain", 65, 4097, heightmap.getHeightMap());
//
//        Material mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
//        mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/GROUND.jpg"));
//
//        Texture grass = assetManager.loadTexture("Textures/GRASS.jpg");
//        grass.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex1", grass);
//        mat_terrain.setFloat("Tex1Scale", 64f);
//
//        Texture dirt = assetManager.loadTexture("Textures/GROUND.jpg");
//        dirt.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex2", dirt);
//        mat_terrain.setFloat("Tex2Scale", 32f);
//
//        Texture rock = assetManager.loadTexture("Textures/GROUND.jpg");
//        rock.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("Tex3", rock);
//        mat_terrain.setFloat("Tex3Scale", 128f);
//
//        terrain.setMaterial(mat_terrain);
//        terrain.setLocalTranslation(0, -100, 0);
//        terrain.setLocalScale(2f, 1f, 2f);
//
//        rootNode.attachChild(terrain);
//    }
//
//    private void addSky() {
//        TextureKey skyTextureKey = new TextureKey("Textures/Sky.jpg", true);
//        Texture skyTexture = assetManager.loadTexture(skyTextureKey);
//        Spatial sky = SkyFactory.createSky(assetManager, skyTexture, SkyFactory.EnvMapType.EquirectMap);
//        rootNode.attachChild(sky);
//    }
//
//    @Override
//    public void simpleUpdate(float tpf) {
//        inputManager.setCursorVisible(true);
//
//        if (isMovingForward) {
//            // Calculate the forward direction based on the mignode's current local rotation
//            Vector3f forwardDir = mignode.getLocalRotation().mult(Vector3f.UNIT_Z);
//            mignode.move(forwardDir.mult(moveSpeed * tpf));
//        }
//
//        if (isMovingDown) {
//            Quaternion facedown = new Quaternion();
//            facedown.fromAngleAxis(FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_X);
//            mignode.rotate(facedown);
//        }
//
//        if (isMovingUp) {
//            Quaternion faceup = new Quaternion();
//            faceup.fromAngleAxis(-FastMath.PI / 2 * tpf * 1.5f, Vector3f.UNIT_X);
//            mignode.rotate(faceup);
//        }
//
//        if (isTiltingLeft) {
//            Quaternion tilt = new Quaternion();
//            tilt.fromAngleAxis(-FastMath.PI / 2 * tpf * 1.5f, new Vector3f(0, 0, 1));
//            mignode.rotate(tilt);
//            Vector3f currentPos = mignode.getLocalTranslation();
//            mignode.setLocalTranslation(currentPos.add(moveSpeed * tpf / 15, 0, 0));
//        }
//
//        if (isTiltingRight) {
//            Quaternion tilt = new Quaternion();
//            tilt.fromAngleAxis(FastMath.PI / 2 * tpf * 1.5f, new Vector3f(0, 0, 1));
//            mignode.rotate(tilt);
//            Vector3f currentPos = mignode.getLocalTranslation();
//            mignode.setLocalTranslation(currentPos.add(-moveSpeed * tpf / 15, 0, 0));
//        }
//
////        if (!isTiltingLeft && !isTiltingRight && !isMovingUp && !isMovingDown && !mignode.getLocalRotation().equals(originalRotation)) {
////            Quaternion currentRotation = mignode.getLocalRotation();
////            Quaternion interpolatedRotation = new Quaternion();
////            interpolatedRotation.slerp(currentRotation, originalRotation, tiltSpeed * tpf);
////            mignode.setLocalRotation(interpolatedRotation);
////        }
// Quaternion camLookAtRotation = new Quaternion();
//        camLookAtRotation.lookAt(mignode.getLocalTranslation(), Vector3f.UNIT_Y);
//        camNode.setLocalRotation(camLookAtRotation);
//        camNode.setLocalTranslation(mig.getLocalTranslation());
//
//        
//    }
//
//    @Override
//    public void simpleRender(RenderManager rm) {}
//}
