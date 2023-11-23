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
  //val velocity = Vec3 ()

  val modelMatrix by Mat4()
  val modelMatrixInverse by Mat4()
  val orientationMatrix by Mat4()

  var parent : GameObject? = null

  init { 
    addComponentsAndGatherUniforms(*meshes)
    orientationMatrix.set()
  }

  fun update() {
    modelMatrix.set().
      scale(scale).
       rotate(roll).
       rotate(yaw, 1.0f, 0.0f, 0.0f)
    
    parent?.let{ parent -> 
      modelMatrix *= parent.orientationMatrix
    }

    modelMatrix.translate(position)

    parent?.let{ parent -> 
      modelMatrix *= parent.modelMatrix
    }

    modelMatrixInverse.set(modelMatrix).invert()

  }

   open fun move(
      dt : Float = 0.016666f,
      t : Float = 0.0f,
      keysPressed : Set<String> = emptySet<String>(),
      gameObjects : List<GameObject> = emptyList<GameObject>()
      ) : Boolean {
    return true;
  }

}
