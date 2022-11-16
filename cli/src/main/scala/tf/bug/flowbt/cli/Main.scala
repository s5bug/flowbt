package tf.bug.flowbt.cli

import cats.effect._
import cats.syntax.all._
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import java.nio.file.{Path, Paths}
import scribe.{Level, Logger, Scribe}
import scribe.cats._

object Main extends CommandIOApp("flowbt", "Flowgram Build Tool") {

  override def main: Opts[IO[ExitCode]] = {
    val buildSheet = Opts.argument[Path]("build sheet")
    val generatedFolder = Opts.argument[Path]("generated folder")
    val verbose = Opts.flag("verbose", "Enable verbose logging", "v").orFalse
    (buildSheet, generatedFolder, verbose).mapN { (bs, gf, v) =>
      val level = if(v) Level.Trace else Level.Info
      IO {
        Logger.root
          .clearModifiers()
          .clearHandlers()
          .withHandler(minimumLevel = Some(level))
          .replace()
      } *> program[IO](bs, gf)
    }
  }

  def program[F[_]](buildSheet: Path, generatedFolder: Path)(implicit sync: Sync[F], scribe: Scribe[F]): F[ExitCode] = for {
    _ <- scribe.trace(s"Entered program(buildSheet = $buildSheet, generatedFolder = $generatedFolder)")
    ec <- sync.delay(Paths.get(".")).map(_.toAbsolutePath).flatMap { absoluteCwd =>
      val absoluteBuildSheet = buildSheet.toAbsolutePath
      val buildSheetHere = absoluteBuildSheet.startsWith(absoluteCwd)
      val absoluteGeneratedFolder = generatedFolder.toAbsolutePath
      val generatedFolderHere = absoluteGeneratedFolder.startsWith(absoluteCwd)

      scribe.debug(s"Starting flowbt")
      scribe.debug(s"CWD: $absoluteCwd")
      scribe.debug(s"Build Sheet: $absoluteBuildSheet")
      scribe.debug(s"Generated Folder: $absoluteGeneratedFolder")

      if (buildSheetHere && generatedFolderHere) {
        sync.pure(ExitCode.Success)
      } else {
        val errorStrings = List(
          Option.when(!buildSheetHere)("Build sheet not beneath current working directory."),
          Option.when(!generatedFolderHere)("Generated folder not beneath current working directory.")
        ).flatten
        errorStrings.traverse_(scribe.error(_)).as(ExitCode.Error)
      }
    }
    _ <- scribe.trace(s"Exited program(buildSheet = $buildSheet, generatedFolder = $generatedFolder)")
  } yield ec
}
