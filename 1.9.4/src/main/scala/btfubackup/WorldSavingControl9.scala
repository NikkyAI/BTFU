package btfubackup

import net.minecraftforge.common.ForgeVersion
import net.minecraftforge.fml.common.FMLCommonHandler

object WorldSavingControl9 extends WorldSavingControl {
  override def realSaveTasks(t: Int) = {
    t match {
      case 1 => // save-off and save-all
        FMLCommonHandler.instance.getMinecraftServerInstance.worldServers.foreach { worldserver =>
          if (worldserver != null) {
            worldserver.disableLevelSaving = false
            worldserver.saveAllChunks(true, null)
            if (ForgeVersion.buildVersion >= 1961) try { // see https://github.com/MinecraftForge/FML/issues/679
              worldserver.saveChunkData()
            } catch {
              case e: Throwable => BTFU.log.warn("Exception from WorldServer.saveChunkData", e)
            }

            worldserver.disableLevelSaving = true
          }
        }
      case 2 => // save-on
        FMLCommonHandler.instance.getMinecraftServerInstance.worldServers.foreach { worldserver =>
          if (worldserver != null) {
            worldserver.disableLevelSaving = false
          }
        }
      case _ => throw new IllegalArgumentException(s"internal error in WorldSavingControl: invalid task: $t")
    }
  }

  override def getActivePlayerCount: Int = FMLCommonHandler.instance().getMinecraftServerInstance.getCurrentPlayerCount
}
