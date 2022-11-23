package tf.bug.flowbt.cli

import cats.effect._
import cats.syntax.all._
import com.monovore.decline.{Command, Opts}
import java.nio.file.{Path, Paths}
import scribe.{Level, Logger, Scribe}
import scribe.cats._

object Main extends IOApp {

  case class Arguments(
    buildSheet: Path,
    generatedFolder: Path,
    verbose: Boolean
  )

  override def run(args: List[String]): IO[ExitCode] = {
    val cmd = Command("flowbt", "Flowgram Build Tool", helpFlag = true)(opts)
    cmd.parse(args) match {
      case Left(help) => std.Console[IO].errorln(help).as(ExitCode.Error)
      case Right(args) =>
        val level = if(args.verbose) Level.Trace else Level.Info
        val replaceLogger = IO {
          Logger.root
            .clearModifiers()
            .clearHandlers()
            .withHandler(minimumLevel = Some(level))
            .replace()
        }
        replaceLogger *> program[IO](args.buildSheet, args.generatedFolder)
    }
  }

  def opts: Opts[Arguments] = {
    val buildSheet = Opts.argument[Path]("build sheet")
    val generatedFolder = Opts.argument[Path]("generated folder")
    val verbose = Opts.flag("verbose", "Enable verbose logging.", "v").orFalse
    (buildSheet, generatedFolder, verbose).mapN(Arguments)
  }

  def program[F[_]](buildSheet: Path, generatedFolder: Path)(implicit sync: Sync[F], scribe: Scribe[F]): F[ExitCode] = for {
    _ <- scribe.trace(s"ENTER program(buildSheet = $buildSheet, generatedFolder = $generatedFolder)")
    ec <- sync.delay(Paths.get(".")).map(_.toAbsolutePath.normalize()).flatMap { absoluteCwd =>
      val absoluteBuildSheet = buildSheet.toAbsolutePath.normalize()
      val buildSheetHere = absoluteBuildSheet.startsWith(absoluteCwd)
      val absoluteGeneratedFolder = generatedFolder.toAbsolutePath.normalize()
      val generatedFolderHere = absoluteGeneratedFolder.startsWith(absoluteCwd)

      val contextLogs: F[Unit] = List(
        s"Starting flowbt",
        s"CWD: $absoluteCwd",
        s"Build Sheet: $absoluteBuildSheet",
        s"Generated Folder: $absoluteGeneratedFolder"
      ).traverse_(scribe.debug(_))

      contextLogs.flatMap { _ =>
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
    }
    _ <- scribe.trace(s"LEAVE program(buildSheet = $buildSheet, generatedFolder = $generatedFolder)")
  } yield ec
}
