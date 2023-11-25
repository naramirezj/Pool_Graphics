import org.w3c.dom.HTMLCanvasElement
import org.khronos.webgl.WebGLRenderingContext as GL //# GL# we need this for the constants declared ˙HUN˙ a constansok miatt kell
import kotlin.js.Date
import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec1
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Vec3
import vision.gears.webglmath.Vec4
import vision.gears.webglmath.Mat4
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.cos

class Scene (
  val gl : WebGL2RenderingContext)  : UniformProvider("scene") {

  var firstUpdate = true

  val vsTextured = Shader(gl, GL.VERTEX_SHADER, "textured-vs.glsl")
  val fsTextured = Shader(gl, GL.FRAGMENT_SHADER, "textured-fs.glsl")
  val texturedProgram = Program(gl, vsTextured, fsTextured)

  val texturedQuadGeometry = TexturedQuadGeometry(gl)

  val vsQuad = Shader(gl, GL.VERTEX_SHADER, "quad-vs.glsl")
  val fsBackground = Shader(gl, GL.FRAGMENT_SHADER, "background-fs.glsl")
  val backgroundProgram = Program(gl, vsQuad, fsBackground)
  val skyCubeTexture = TextureCube(gl,
      "media/posx512.jpg", "media/negx512.jpg",
      "media/posy512.jpg", "media/negy512.jpg",
      "media/posz512.jpg", "media/negz512.jpg"
    )  
  val backgroundMaterial = Material(backgroundProgram).apply{
    this["envTexture"]?.set(skyCubeTexture)
  }
  val backgroundMesh = Mesh(backgroundMaterial, texturedQuadGeometry)

  // LABTODO: load geometries from the JSON file, create Meshes  
  val sphereTexture = Texture2D(gl, "media/sphere/ball.png")
  val sphereMaterials = arrayOf(
    Material(texturedProgram).apply {
      this["colorTexture"]?.set(sphereTexture)
      this["envTexture"]?.set(skyCubeTexture)
    }
  )
  val sphereMaterials2 = arrayOf(
    Material(texturedProgram).apply {
      this["colorTexture"]?.set(sphereTexture)
      this["envTexture"]?.set(skyCubeTexture)
    }
  )

  

  val jsonLoader = JsonLoader()
  val sphereGeometries = jsonLoader.loadGeometries(gl,
    "media/sphere/sphere.json")

  val sphereMeshes = 
    (sphereMaterials zip sphereGeometries).map{ Mesh(it.first, it.second) }.toTypedArray()

  val sphereMeshes2 = (sphereMaterials zip sphereGeometries).map{ Mesh(it.first, it.second) }.toTypedArray()

  val lights = Array<Light>(2) { Light(it) }
  init{
    lights[0].position.set(1.0f, 1.0f, 1.0f, 0.0f).normalize();
    lights[0].powerDensity.set(1.0f, 1.0f, 1.0f);
    lights[1].position.set(10.0f, 10.0f, 10.0f, 1.0f).normalize();
    lights[1].powerDensity.set(0.0f, 0.0f, 1.0f);
  }


  val gameObjects = ArrayList<GameObject>()

  val avatar = PhysicsGameObject(*sphereMeshes).apply{
    position.set(0f, 0f, 0f)
    scale.set(1f, 1f, 1f)
  }

  val sphere = object: PhysicsGameObject(*sphereMeshes2){
    override fun control (
      dt : Float,
      t : Float,
      keysPressed : Set<String>,
      gameObjects : List<GameObject>) : Boolean
    {
    //roll += 0.1f
    
    force.set (-2.0f * sin(t), 0.0f, 5.0f * cos(2.0f * t));
    return true;
    }
    override fun move(
      dt : Float,
      t : Float,
      keysPressed : Set<String>,
      gameObjects : List<GameObject>
      ) : Boolean {

    control (dt, t, keysPressed, gameObjects)

    acceleration.set(force * invMass)
    velocity += acceleration * dt
    position += velocity * dt

   // Calculate angular velocity based on the velocity magnitude
      val speed = velocity.length() // Magnitude of velocity vector
      val rotationFactor = -0.3f // Adjust this factor for desired rotation speed
      val angularSpeed = speed * rotationFactor

        // Update angular velocity
      angularVelocity = angularSpeed

        // Adjust the rotation based on the velocity direction
        if (speed > 0) {
            val axis = Vec3(0.0f, 1.0f, 0.0f) // Define the rotation axis
            orientationMatrix.rotate(angularSpeed * dt, velocity.clone().normalize().cross(axis))
        }
    //orientationMatrix.rotate(-0.04f, velocity.clone().normalize().cross(Vec3(0.0f, 1.0f, 0.0f)))
    return true;
  }}.apply{
    position.set(5f, 0f, 2f)
    scale.set(1f, 1f, 1f)
    restitutionCoeff = 0.9f
  }

  init {
    // LABTODO: create and add game object using meshes loaded from JSON
    gameObjects += GameObject(backgroundMesh)
    gameObjects += avatar
    gameObjects += sphere
  }

  // LABTODO: replace with 3D camera
  val camera = PerspectiveCamera().apply{
    update()
  }

  fun resize(canvas : HTMLCanvasElement) {
    gl.viewport(0, 0, canvas.width, canvas.height)//#viewport# tell the rasterizer which part of the canvas to draw to ˙HUN˙ a raszterizáló ide rajzoljon
    camera.setAspectRatio(canvas.width.toFloat()/canvas.height)
  }

  val timeAtFirstFrame = Date().getTime()
  var timeAtLastFrame =  timeAtFirstFrame

  init{
    //LABTODO: enable depth test
    gl.enable(GL.DEPTH_TEST)
    addComponentsAndGatherUniforms()
  }

  @Suppress("UNUSED_PARAMETER")
  fun update(keysPressed : Set<String>) {
    val timeAtThisFrame = Date().getTime() 
    val dt = (timeAtThisFrame - timeAtLastFrame).toFloat() / 1000.0f
    val t = (timeAtThisFrame - timeAtFirstFrame).toFloat() / 1000.0f
    timeAtLastFrame = timeAtThisFrame

    //LABTODO: move camera
    camera.move(dt, keysPressed)
    lights[0].position.set(sin(t), cos(t), cos(2f*t), 0f).normalize()


    if (avatar is PhysicsGameObject) {
    val dampingFactor = 0.95f // Adjust this factor for the desired damping effect
    avatar.force *= dampingFactor // Gradually reduce the force
    } 

    
    gl.clearColor(0.3f, 0.0f, 0.3f, 1.0f)//## red, green, blue, alpha in [0, 1]
    gl.clearDepth(1.0f)//## will be useful in 3D ˙HUN˙ 3D-ben lesz hasznos
    gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)//#or# bitwise OR of flags

    gl.enable(GL.BLEND)
    gl.blendFunc(
      GL.SRC_ALPHA,
      GL.ONE_MINUS_SRC_ALPHA)

    gameObjects.forEach{ it.move(dt, t, keysPressed, gameObjects) }

    gameObjects.forEach{ it.update() }
    gameObjects.forEach{ it.draw(this, camera, *lights) }
  }
}
