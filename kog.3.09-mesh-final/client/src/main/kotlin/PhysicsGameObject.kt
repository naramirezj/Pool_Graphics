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
  var isFirstUpdate = true

  open fun control (
      dt : Float = 0.016666f,
      t : Float = 0.0f,
      keysPressed : Set<String> = emptySet<String>(),
      gameObjects : List<GameObject> = emptyList<GameObject>()) : Boolean
  {
    
    //force.set (4.0f * sin(t), 0.0f, 10.0f * cos(2.0f * t));
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
      val rotationFactor = 0.7f // Adjust this factor for desired rotation speed
      val angularSpeed = speed * rotationFactor

        // Update angular velocity
      angularVelocity = angularSpeed

        // Adjust the rotation based on the velocity direction
        if (speed > 0) {
            val axis = Vec3(0.0f, 1.0f, 0.0f) // Define the rotation axis
            orientationMatrix.rotate(-angularSpeed * dt, velocity.clone().normalize().cross(axis))
        }
    collisionMove(dt, t, keysPressed, gameObjects)

    return true;
  }

  open fun collisionMove (
        dt : Float,
        t : Float,
        keysPressed : Set<String>,
        gameObjects : List<GameObject>) : Boolean
    {
    gameObjects.forEach { obj ->
        if (obj is PhysicsGameObject && obj != this) {
            val diff = position - obj.position
            val dist = diff.length()
            if (dist < radius + obj.radius) {
                val collisionNormal = diff.normalize()
                // Resolve overlap (push objects apart)
                val pushDistance = (radius + obj.radius - dist) * 0.7f
                position += collisionNormal * pushDistance
                obj.position -= collisionNormal * pushDistance

                // Calculate relative velocity
                val relativeVelocity = obj.velocity - velocity

                // Calculate impulse and update velocities
                val impulse = collisionNormal * (1.0f + restitutionCoeff) * (collisionNormal dot relativeVelocity) / (invMass + obj.invMass)
                velocity += impulse * invMass
                obj.velocity -= impulse * obj.invMass
                obj.force.x += 0.3f
                force.x -= 0.1f
            }
        }
    }
    return true
  }



open fun generateUniqueID(): String {
    return buildString {
        repeat(16) {
            append(('a'..'f').random())
        }
    }
}


}
