package ie.itcarlow.lonelyroadahead;

import ie.itcarlow.lonelyroadahead.SceneManager.SceneType;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.util.color.Color;

import com.badlogic.gdx.math.Vector2;

public class GameScene extends BaseScene {
	private Text scoreText;
	private HUD gameHUD;
	private PhysicsWorld physicsWorld;

	private void createPhysics()
	{
		physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -17), false); 
		registerUpdateHandler(physicsWorld);
	}

	private void createHUD()
	{
	    gameHUD = new HUD();
	    
	    // CREATE SCORE TEXT
	    scoreText = new Text(20, 420, resourcesManager.font, "Score: 0123456789", vbom);
	    //scoreText.setAnchorCenter(0, 0);    
	    scoreText.setText("Score: 0");
	    gameHUD.attachChild(scoreText);
	    
	    camera.setHUD(gameHUD);
	}
	
	@Override
	public void createScene()
	{
	    createBackground();
	    createHUD();
	    createPhysics();
	}

	@Override
	public void onBackKeyPressed() {
		 SceneManager.getInstance().loadMenuScene(engine);
		
	}

	@Override
	public SceneType getSceneType() {
		return SceneType.SCENE_GAME;
	}

	@Override
	public void disposeScene()
	{
	    camera.setHUD(null);
	    camera.setCenter(400, 240);

	    // TODO code responsible for disposing scene
	    // removing all game scene objects.
	}
	
	
	private void createBackground()
	{
	    setBackground(new Background(Color.BLUE));
	}

}
