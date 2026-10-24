package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DroneFrameId
import com.example.model.HitZone
import com.example.model.PlayerDroneEquipment
import com.example.model.WarheadType
import com.example.physics.FlightPhysics
import com.example.physics.Vector3D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ПАНЦИРЬ: FPV", appName)
  }

  @Test
  fun `test flight physics initialization and thrust`() {
    val eq = PlayerDroneEquipment(
      frame = DroneFrameId.SHMEL_7,
      warhead = WarheadType.PG_7VL
    )
    val physics = FlightPhysics(eq)
    assertTrue("Initial altitude should be greater than zero", physics.position.y > 0f)
    assertTrue("Drone mass should be positive", physics.totalMassKg > 1.0f)
    assertTrue("Max thrust should exceed gravity", physics.maxThrustN > (physics.totalMassKg * 9.81f))
  }

  @Test
  fun `test hit zone multiplier calculation`() {
    assertTrue("Radar dish hit should yield critical bonus", HitZone.RADAR_DISH.multiplier > 2.0f)
    assertTrue("Ammo rack detonation should yield high damage", HitZone.AMMO_RACK.multiplier >= 3.0f)
    assertTrue("Reactive armor should reduce damage", HitZone.REACTIVE_ARMOR.multiplier < 1.0f)
  }
}
