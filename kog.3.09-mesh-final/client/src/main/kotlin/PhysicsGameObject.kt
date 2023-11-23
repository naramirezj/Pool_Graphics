import vision.gears.webglmath.*
import kotlin.math.*
import kotlin.random.Random

open class PhysicsGameObject(
  vararg meshes : Mesh
   ) : GameObject(*meshes) {

  val velocity = Vec3 ()
  val acceleration = Vec3 ()
  val force = Vec3 ()
  
  var angularVelocity = 0.0f
  var angularAcceleration = 0.0f
  var torque = 0.0f

  var id: String = generateUniqueID()

  val radius = 1.0f
  var invMass = 1.0f
  val radialInvMass = 1.0f
  var restitutionCoeff = 0.8f

  open fun control (
      dt : Float = 0.016666f,
      t : Float = 0.0f,
      keysPressed : Set<String> = emptySet<String>(),
      gameObjects : List<GameObject> = emptyList<GameObject>()) : Boolean
  {
    roll += 0.1f
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

    angularAcceleration = torque * radialInvMass
    angularVelocity += angularAcceleration * dt

    velocity *= exp (-dt)
    angularVelocity *= exp (-dt)
    //creating the cross product of the y axis and the 
    //collisionMove(dt, t, keysPressed, gameObjects)
    
    //val y_axis = Vec3(0.0f, position.y, 0.0f)

    orientationMatrix.rotate(0.02f, velocity.cross(position))
    return true;
  }

  open fun collisionMove (
        dt : Float,
        t : Float,
        keysPressed : Set<String>,
        gameObjects : List<GameObject>) : Boolean
    {

      gameObjects.forEach {
        if (it is PhysicsGameObject) {
            val diff = position - it.position
            val dist = diff.length()
            if (dist < radius + it.radius) {
              val collisionNormal = diff.normalize()
              position += collisionNormal * invMass / (invMass + it.invMass) * dist * 0.01f
              it.position += collisionNormal * it.invMass / (invMass + it.invMass) * dist * -0.01f
              
              val collisionTangent = Vec3(-collisionNormal.y, collisionNormal.x, 0.0f)
              val relativeVelocity = velocity - it.velocity
              
              val impulseLength = collisionNormal.dot(relativeVelocity) / (invMass + it.invMass) * (1.0f + restitutionCoeff)
              val restitution = collisionNormal * impulseLength
              velocity -= restitution * invMass
              it.velocity += restitution * it.invMass       
          }
        }
      }

      return true;
  }



open fun generateUniqueID(): String {
    return buildString {
        repeat(16) {
            append(('a'..'f').random())
        }
    }
}


}
