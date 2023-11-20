import vision.gears.webglmath.*
import kotlin.math.exp
import kotlin.math.PI
import kotlin.math.floor

open class GameObject(
  vararg val meshes : Mesh
   ) : UniformProvider("gameObject") {

  val position = Vec3()
  var roll = 0.0f
  var yaw = 0.0f
  val scale = Vec3(1.0f, 1.0f, 1.0f)

  val modelMatrix by Mat4()
  val modelMatrixInverse by Mat4()

  var parent : GameObject? = null

  init { 
    addComponentsAndGatherUniforms(*meshes)
  }

  fun update() {
    modelMatrix.set().
      scale(scale).
      rotate(roll).
      rotate(yaw, 1.0f, 0.0f, 0.0f).
      translate(position)
    parent?.let{ parent -> 
      modelMatrix *= parent.modelMatrix
    }
    modelMatrixInverse.set(modelMatrix).invert()
  }

  open class Motion(val gameObject : GameObject) {
    open operator fun invoke(
        dt : Float = 0.016666f,
        t : Float = 0.0f,
        keysPressed : Set<String> = emptySet<String>(),
        gameObjects : List<GameObject> = emptyList<GameObject>()
        ) : Boolean {
      gameObject.roll += dt 
      return true
    }
  }
  var move = Motion(this)

}
