package freedsl.filesystem

import freestyle.tagless._
import java.io._

@tagless trait FileSystem {
  def list(path: Path): FS[Vector[File]]
  def _readStream[T](path: Path, f: InputStream => T): FS[T]
  def readStream[T](path: Path)(f: InputStream => T): FS[T] = _readStream[T](path, f)
  def read(path: Path): FS[String] = readStream(path)(is ⇒ io.Source.fromInputStream(is).mkString)
}

case class FileSystemInterpreter() extends FileSystem.Handler[util.Try] {
  def list(p: Path) = util.Try(p.path.listFiles.toVector)

  def _readStream[T](path: Path, f: InputStream => T) =
    util.Try {
      val is = new BufferedInputStream(new FileInputStream(path.path))
      try f(is)
      finally is.close
    }

}

