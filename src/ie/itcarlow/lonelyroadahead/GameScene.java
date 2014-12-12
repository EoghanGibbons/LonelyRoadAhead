package ie.itcarlow.lonelyroadahead;

import java.util.Iterator;

import ie.itcarlow.lonelyroadahead.SceneManager.SceneType;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.color.Color;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

public class GameScene extends BaseScene implements IOnSceneTouchListener {
	//---------------------------------------------
    // VARIABLES
    //---------------------------------------------
	private Text scoreText;
	private HUD gameHUD;
	
	private PhysicsWorld physicsWorld;
	
	private BitmapTextureAtlas mTexturePlayer;
	
	private ITextureRegion mPlayerTextureRegion;
	private ITextureRegion mBoundryTextureRegion;
	
	private Sprite player;
	private Sprite platformOne;
	private Sprite platformTwo;
	private Sprite platformThree;
	private Sprite platformFour;
	private Sprite sprSwitch;
	private Sprite exit;

	private boolean hitSwitch = false;
	private boolean canJump = true;
	private boolean createExit = false;
	private boolean movePlatforms = false;
	public boolean gameOver = false;
	public boolean startLevelTwo=false;
	
	private int level = 0;
	
	//---------------------------------------------
    // CONSTRUCTOR
    //---------------------------------------------
	
	@Override
	public void createScene(){
		ResourceManager.gameMusic.play();
    	ResourceManager.gameMusic.setLooping(true);
	    createBackground();
	    //createHUD();
	    createPhysics();
	    createPlayer();
	    createBoundry();
	    createLevelOne();
	    //createLevelTwo();
	    setOnSceneTouchListener(this);
	    //this.engine.registerUpdateHandler(this);
	}

	//---------------------------------------------
    // INITIALIZERS
    //---------------------------------------------
	
	private void createPhysics(){
		physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, 9.81f), false); 
		registerUpdateHandler(physicsWorld);
		physicsWorld.setContactListener(createContactListener());
	}
	
	private void createHUD(){
	    gameHUD = new HUD();
	    
	    // CREATE SCORE TEXT
	    scoreText = new Text(20, 420, resourcesManager.font, "Score: 0123456789", vbom);
	    //scoreText.setAnchorCenter(0, 0);    
	    scoreText.setText("Click Character to jump");
	    gameHUD.attachChild(scoreText);
	    
	    camera.setHUD(gameHUD);
	}
	
	private void createBoundry(){
		final FixtureDef fixDef = PhysicsFactory.createFixtureDef(0f,0f, 1.0f);
		
		BitmapTextureAtlas mTextureBoundryFloor = new BitmapTextureAtlas(engine.getTextureManager(), 800, 10);
		mBoundryTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureBoundryFloor, this.activity, "floor.png", 0, 0);
		mTextureBoundryFloor.load();
		
		Sprite floor = new Sprite(0,470, mBoundryTextureRegion, engine.getVertexBufferObjectManager());
		Body bodyFloor = PhysicsFactory.createBoxBody(physicsWorld, floor, BodyType.StaticBody, fixDef);
		bodyFloor.setUserData("floor");
		floor.setUserData(bodyFloor);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(floor, bodyFloor, true, true));
		attachChild(floor);
		
		Sprite roof = new Sprite(0,0, mBoundryTextureRegion, engine.getVertexBufferObjectManager());
		Body bodyRoof = PhysicsFactory.createBoxBody(physicsWorld, roof, BodyType.StaticBody, fixDef);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(roof, bodyRoof, true, true));
		attachChild(roof);
		
		BitmapTextureAtlas mTextureBoundryWall = new BitmapTextureAtlas(engine.getTextureManager(), 10, 460);
		mBoundryTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureBoundryWall, this.activity, "wall.png", 0, 0);
		mTextureBoundryWall.load();
		
		Sprite wallLeft = new Sprite(0,10, mBoundryTextureRegion, engine.getVertexBufferObjectManager());
		Body bodyWallLeft = PhysicsFactory.createBoxBody(physicsWorld, wallLeft, BodyType.StaticBody, fixDef);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(wallLeft, bodyWallLeft, true, true));
		attachChild(wallLeft);
		
		Sprite wallRight = new Sprite(790,10, mBoundryTextureRegion, engine.getVertexBufferObjectManager());
		Body bodyWallRight = PhysicsFactory.createBoxBody(physicsWorld, wallRight, BodyType.StaticBody, fixDef);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(wallRight, bodyWallRight, true, true));
		attachChild(wallRight);
	}
	
	public void createPlayer(){
		mTexturePlayer = new BitmapTextureAtlas(engine.getTextureManager(), 40, 40);  
		mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTexturePlayer, this.activity, "player.png", 0, 0);
        mTexturePlayer.load();
        player = new Sprite(20, 430,  mPlayerTextureRegion, engine.getVertexBufferObjectManager());
        createPhysicsBodies();
        this.attachChild(player);
	}
	
	private void createPhysicsBodies(){
		final FixtureDef fixDef = PhysicsFactory.createFixtureDef(1.5f,0f, 0.3f);
		
		Body body = PhysicsFactory.createBoxBody(physicsWorld, player, BodyType.DynamicBody, fixDef);
		body.setFixedRotation(true);
		body.setUserData("player");
    	player.setUserData(body); 
    	physicsWorld.registerPhysicsConnector(new PhysicsConnector(player, body, true, true));
	 }
	
	private void createBackground()
	{
	    setBackground(new Background(Color.WHITE));
	}
	
	private ContactListener createContactListener()
    {
        ContactListener contactListener = new ContactListener()
        {
            public void beginContact(Contact contact)
            {
                final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();

                if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
                {
                    if (x2.getBody().getUserData().equals("player")|| (x1.getBody().getUserData().equals("player")))
                    {
                        canJump = true;
                    }
                    
                    if ((x2.getBody().getUserData().equals("player") || x2.getBody().getUserData().equals("switch")) && 
                    		((x1.getBody().getUserData().equals("player") || x1.getBody().getUserData().equals("switch")))){
                    	
                    	hitSwitch = true;
                    	
                    	Iterator <Body> iter = physicsWorld.getBodies();
                    	
                    	for (int x = 0; x < physicsWorld.getBodyCount(); x++){
                    	
                    		Body checkBody = iter.next();
                    		if (checkBody.getUserData() == "platform"){
                    			checkBody.setType(BodyType.DynamicBody);
                    		}
                    	}
                    }
                    
                    if ((x2.getBody().getUserData().equals("player") || x2.getBody().getUserData().equals("switchTwo")) && 
                    		((x1.getBody().getUserData().equals("player") || x1.getBody().getUserData().equals("switchTwo")))){
                    	createExit = true;
                    	
                    	Iterator <Body> iter = physicsWorld.getBodies();
                    	
                    	for (int x = 0; x < physicsWorld.getBodyCount(); x++){
                    	
                    		Body checkBody = iter.next();
                    		if (checkBody.getUserData() == "platform"){
                    			movePlatforms = true;
                    		}
                    	}
                    }
                    
                    if ((x2.getBody().getUserData().equals("player") || x2.getBody().getUserData().equals("exit")) && 
                    		((x1.getBody().getUserData().equals("player") || x1.getBody().getUserData().equals("exit")))){
                    	if (level < 2){
                    		startLevelTwo = true;
                    	}
                    	else {
                    		gameOver = true;
                    	}
                    }
                }
            }

            public void endContact(Contact contact)
            {
                final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();

                if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
                {
                    if (x2.getBody().getUserData().equals("player") || (x1.getBody().getUserData().equals("player")))
                    {
                        canJump = false;
                    }
                }
            }

            public void preSolve(Contact contact, Manifold oldManifold)
            {

            }

            public void postSolve(Contact contact, ContactImpulse impulse)
            {

            }
        };
        return contactListener;
    }
	
	public void createExit(float exitX, float exitY){
		BitmapTextureAtlas exitTexture = new BitmapTextureAtlas(engine.getTextureManager(), 40,40);
		ITextureRegion exitTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(exitTexture, activity, "exit.png", 0, 0);
		exitTexture.load();
		exit = new Sprite(exitX,exitY, exitTextureRegion, engine.getVertexBufferObjectManager());
		
		final FixtureDef fixDef = PhysicsFactory.createFixtureDef(1.5f,0f, 0.3f);
		Body exitBody = PhysicsFactory.createBoxBody(physicsWorld, exit, BodyType.StaticBody, fixDef);
		exitBody.setFixedRotation(true);
		exitBody.setUserData("exit");
    	exit.setUserData(exitBody); 
    	physicsWorld.registerPhysicsConnector(new PhysicsConnector(exit, exitBody, true, true));
    	this.attachChild(exit);
	}
	
	//---------------------------------------------
    // CLASS LOGIC
    //---------------------------------------------
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		float touchFromRight = pSceneTouchEvent.getX() - (player.getX() + player.getWidth());
		float touchFromLeft = pSceneTouchEvent.getX() - player.getX();
		float touchFromBottom = pSceneTouchEvent.getY() - (player.getY() + player.getHeight());
		float touchFromTop = pSceneTouchEvent.getY() - player.getY();
		Body bodyPlayer = (Body) player.getUserData();
		//Touch to the right of the player
		if ((touchFromRight > 0) && (touchFromRight < 800)){
			if (touchFromRight > 30){
				bodyPlayer.setLinearVelocity(5, bodyPlayer.getLinearVelocity().y);
			}
			else{
				bodyPlayer.setLinearVelocity(1, bodyPlayer.getLinearVelocity().y);
			}
		}
		
		//Touch to the left of the player
		else if ((touchFromLeft < 0) && (touchFromLeft > -800)){
			
			if (touchFromLeft < -30){
				bodyPlayer.setLinearVelocity(-5, bodyPlayer.getLinearVelocity().y);
			}
			else{
				bodyPlayer.setLinearVelocity(-1, bodyPlayer.getLinearVelocity().y);
			}
		}
	
		//Touch on the player
		else if ((touchFromLeft > 0) && (touchFromRight < 0) && (touchFromTop > 0) && (touchFromBottom < 0) && (canJump)){
			bodyPlayer.setLinearVelocity(bodyPlayer.getLinearVelocity().x, -9);
			ResourceManager.jumpSound.play();
		}
		return false;
	}
	
	@Override
	public void disposeScene()
	{
	    camera.setHUD(null);
	    camera.setCenter(400, 240);
	    ResourceManager.gameMusic.stop();
	}
	
	@Override
	public void onBackKeyPressed() {
		 SceneManager.getInstance().loadMenuScene(engine);
	}
	
	public void levelOneNewPlatformPositioning(){
		PhysicsConnector physicsConnector = physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(platformOne);
		// Unregister the physics connector
		physicsWorld.unregisterPhysicsConnector(physicsConnector);
		// Destroy the body
		physicsWorld.destroyBody(physicsConnector.getBody());
		
		final FixtureDef fixDef = PhysicsFactory.createFixtureDef(1.5f,0f, 0.3f);
		
		platformOne.setPosition(600, 400);
		Body body = PhysicsFactory.createBoxBody(physicsWorld, platformOne, BodyType.StaticBody, fixDef);
		body.setFixedRotation(true);
		body.setUserData("platform");
		platformOne.setUserData(body); 
    	physicsWorld.registerPhysicsConnector(new PhysicsConnector(platformOne, body, true, true));
    	
    	
    	physicsConnector = physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(platformTwo);
		// Unregister the physics connector
		physicsWorld.unregisterPhysicsConnector(physicsConnector);
		// Destroy the body
		physicsWorld.destroyBody(physicsConnector.getBody());
		
		platformTwo.setPosition(400, 300);
		Body bodyTwo = PhysicsFactory.createBoxBody(physicsWorld, platformTwo, BodyType.StaticBody, fixDef);
		bodyTwo.setFixedRotation(true);
		bodyTwo.setUserData("platform");
		platformTwo.setUserData(bodyTwo); 
    	physicsWorld.registerPhysicsConnector(new PhysicsConnector(platformTwo, bodyTwo, true, true));
    	
    	
    	physicsConnector = physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(platformThree);
		// Unregister the physics connector
		physicsWorld.unregisterPhysicsConnector(physicsConnector);
		// Destroy the body
		physicsWorld.destroyBody(physicsConnector.getBody());
		
		platformThree.setPosition(300, 200);
		Body bodyThree = PhysicsFactory.createBoxBody(physicsWorld, platformThree, BodyType.StaticBody, fixDef);
		bodyThree.setFixedRotation(true);
		bodyThree.setUserData("platform");
		platformThree.setUserData(bodyThree); 
    	physicsWorld.registerPhysicsConnector(new PhysicsConnector(platformThree, bodyThree, true, true));
    	
    	
    	physicsConnector = physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(platformFour);
		// Unregister the physics connector
		physicsWorld.unregisterPhysicsConnector(physicsConnector);
		// Destroy the body
		physicsWorld.destroyBody(physicsConnector.getBody());
		
		platformFour.setPosition(20, 80);
		Body bodyFour = PhysicsFactory.createBoxBody(physicsWorld, platformFour, BodyType.StaticBody, fixDef);
		bodyFour.setFixedRotation(true);
		bodyFour.setUserData("platform");
		platformFour.setUserData(bodyFour); 
    	physicsWorld.registerPhysicsConnector(new PhysicsConnector(platformFour, bodyFour, true, true));
	
	}
	
	//---------------------------------------------
    // GETTERS AND SETTERS
    //---------------------------------------------
	
	public boolean getHitSwitchStatus() {
		return hitSwitch;
	}
	
	public void setHitSwitchStatus(boolean pStatus) {
		hitSwitch = pStatus;
	}
	
	@Override
	public SceneType getSceneType() {
		return SceneType.SCENE_GAME;
	}
	
	public void setNewSwitchBody(float newX, float newY){
		PhysicsConnector physicsConnector = physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(sprSwitch);
		// Unregister the physics connector
		physicsWorld.unregisterPhysicsConnector(physicsConnector);
		// Destroy the body
		physicsWorld.destroyBody(physicsConnector.getBody());
		
		final FixtureDef fixDef = PhysicsFactory.createFixtureDef(1.5f,0f, 0.3f);
		
		sprSwitch.setPosition(newX, newY);
		Body body = PhysicsFactory.createBoxBody(physicsWorld, sprSwitch, BodyType.StaticBody, fixDef);
		body.setFixedRotation(true);
		body.setUserData("switchTwo");
		sprSwitch.setUserData(body); 
    	physicsWorld.registerPhysicsConnector(new PhysicsConnector(sprSwitch, body, true, true));
	}
	
	public boolean getCreateExit(){
		return createExit;
	}
	
	public void setCreateExit(boolean pStatus){
		createExit = pStatus;
	}

	public boolean getPlatformMoveStatus(){
		return movePlatforms;
	}
	
	public void setPlatformMoveStatus(boolean pStatus){
		movePlatforms = pStatus;
	}
	
	//---------------------------------------------
    // LEVEL CREATION
    //---------------------------------------------
	
	public void createLevelOne(){

		final FixtureDef fixDef = PhysicsFactory.createFixtureDef(0f,0f, 1.0f);
		//small platforms
		BitmapTextureAtlas mTexturePlatform = new BitmapTextureAtlas(engine.getTextureManager(), 90, 30);
		ITextureRegion mTextureRegionPlatform = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTexturePlatform, activity, "platform.png", 0, 0);
		mTexturePlatform.load();
		// large platform
		BitmapTextureAtlas mTextureLargePlatform = new BitmapTextureAtlas(engine.getTextureManager(), 180, 30);
		ITextureRegion mTextureRegionLargePlatform = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureLargePlatform, activity, "largePlatform.png", 0, 0);
		mTextureLargePlatform.load();
		//Switch
		BitmapTextureAtlas mTextureSwitch = new BitmapTextureAtlas(engine.getTextureManager(), 40, 20);
		ITextureRegion mTextureRegionSwitch = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureSwitch, activity, "switch.png", 0, 0);
		mTextureSwitch.load();
		
		platformOne = new Sprite(200, 350, mTextureRegionPlatform, engine.getVertexBufferObjectManager());
		Body bodyPlatformOne = PhysicsFactory.createBoxBody(physicsWorld, platformOne, BodyType.StaticBody, fixDef);
		bodyPlatformOne.setUserData("platform");
		platformOne.setUserData(bodyPlatformOne);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(platformOne, bodyPlatformOne, true, true));
		attachChild(platformOne);
		
		platformTwo = new Sprite(350, 270, mTextureRegionPlatform, engine.getVertexBufferObjectManager());
		Body bodyPlatformTwo = PhysicsFactory.createBoxBody(physicsWorld, platformTwo, BodyType.StaticBody, fixDef);
		bodyPlatformTwo.setUserData("platform");
		platformTwo.setUserData(bodyPlatformTwo);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(platformTwo, bodyPlatformTwo, true, true));
		attachChild(platformTwo);
		
		platformThree= new Sprite(500, 190, mTextureRegionPlatform, engine.getVertexBufferObjectManager());
		Body bodyPlatformThree = PhysicsFactory.createBoxBody(physicsWorld, platformThree, BodyType.StaticBody, fixDef);
		bodyPlatformThree.setUserData("platform");
		platformThree.setUserData(bodyPlatformThree);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(platformThree, bodyPlatformThree, true, true));
		attachChild(platformThree);
		
		platformFour = new Sprite(600, 140, mTextureRegionLargePlatform, engine.getVertexBufferObjectManager());
		Body bodyPlatformFour = PhysicsFactory.createBoxBody(physicsWorld, platformFour, BodyType.StaticBody, fixDef);
		bodyPlatformFour.setUserData("platform");
		platformFour.setUserData(bodyPlatformFour);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(platformFour, bodyPlatformFour, true, true));
		attachChild(platformFour);
		
		sprSwitch = new Sprite(670, 120, mTextureRegionSwitch, engine.getVertexBufferObjectManager());
		Body bodySwitch = PhysicsFactory.createBoxBody(physicsWorld, sprSwitch, BodyType.StaticBody, fixDef);
		bodySwitch.setUserData("switch");
		sprSwitch.setUserData(bodySwitch);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(sprSwitch, bodySwitch, true, true));
		attachChild(sprSwitch);
		
		level = 1;
	}
	
	public void destroyLevelOne(){
		PhysicsConnector physicsConnector = physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(platformOne);
		// Unregister the physics connector
		physicsWorld.unregisterPhysicsConnector(physicsConnector);
		// Destroy the body
		physicsWorld.destroyBody(physicsConnector.getBody());
		//platformOne.reset();
		detachChild(platformOne);
		platformOne.dispose();
		
		physicsConnector = physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(platformTwo);
		// Unregister the physics connector
		physicsWorld.unregisterPhysicsConnector(physicsConnector);
		// Destroy the body
		physicsWorld.destroyBody(physicsConnector.getBody());
		detachChild(platformTwo);
		platformTwo.dispose();
		
		
		physicsConnector = physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(platformThree);
		// Unregister the physics connector
		physicsWorld.unregisterPhysicsConnector(physicsConnector);
		// Destroy the body
		physicsWorld.destroyBody(physicsConnector.getBody());
		detachChild(platformThree);
		platformThree.dispose();
		
		
		physicsConnector = physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(platformFour);
		// Unregister the physics connector
		physicsWorld.unregisterPhysicsConnector(physicsConnector);
		// Destroy the body
		physicsWorld.destroyBody(physicsConnector.getBody());
		detachChild(platformFour);
		platformFour.dispose();
		
		physicsConnector = physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(exit);
		// Unregister the physics connector
		physicsWorld.unregisterPhysicsConnector(physicsConnector);
		// Destroy the body
		physicsWorld.destroyBody(physicsConnector.getBody());
		detachChild(exit);
		exit.dispose();
		
		physicsConnector = physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(sprSwitch);
		// Unregister the physics connector
		physicsWorld.unregisterPhysicsConnector(physicsConnector);
		// Destroy the body
		physicsWorld.destroyBody(physicsConnector.getBody());
		detachChild(sprSwitch);
		sprSwitch.dispose();
	}
	
	public void createLevelTwo(){
		final FixtureDef fixDef = PhysicsFactory.createFixtureDef(0f,0f, 1.0f);
		//small platforms
		BitmapTextureAtlas mTexturePlatform = new BitmapTextureAtlas(engine.getTextureManager(), 180, 30);
		ITextureRegion mTextureRegionPlatform = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTexturePlatform, activity, "largePlatform.png", 0, 0);
		mTexturePlatform.load();
		//vertical Platoforms
		BitmapTextureAtlas mTextureLeftWall = new BitmapTextureAtlas(engine.getTextureManager(), 20, 350);
		ITextureRegion mTextureRegionLeftWall = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureLeftWall, activity, "mossyWallRight.png", 0, 0);
		mTextureLeftWall.load();
		
		BitmapTextureAtlas mTextureRightWall = new BitmapTextureAtlas(engine.getTextureManager(), 20, 350);
		ITextureRegion mTextureRegionRightWall = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureRightWall, activity, "mossyWallLeft.png", 0, 0);
		mTextureRightWall.load();
		
		platformOne = new Sprite(485, 65, mTextureRegionPlatform, engine.getVertexBufferObjectManager());
		Body bodyPlatformOne = PhysicsFactory.createBoxBody(physicsWorld, platformOne, BodyType.StaticBody, fixDef);
		bodyPlatformOne.setUserData("platform");
		platformOne.setUserData(bodyPlatformOne);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(platformOne, bodyPlatformOne, true, true));
		attachChild(platformOne);
		
		platformTwo = new Sprite(330, 65, mTextureRegionLeftWall, engine.getVertexBufferObjectManager());
		Body bodyPlatformTwo = PhysicsFactory.createBoxBody(physicsWorld, platformTwo, BodyType.StaticBody, fixDef);
		bodyPlatformTwo.setUserData("platform");
		platformTwo.setUserData(bodyPlatformTwo);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(platformTwo, bodyPlatformTwo, true, true));
		attachChild(platformTwo);
		
		platformThree= new Sprite(470, 65, mTextureRegionRightWall, engine.getVertexBufferObjectManager());
		Body bodyPlatformThree = PhysicsFactory.createBoxBody(physicsWorld, platformThree, BodyType.StaticBody, fixDef);
		bodyPlatformThree.setUserData("platform");
		platformThree.setUserData(bodyPlatformThree);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(platformThree, bodyPlatformThree, true, true));
		attachChild(platformThree);
		
		createExit(555, 25);
		
		level = 2;
	}
}