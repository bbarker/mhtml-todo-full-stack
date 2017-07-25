package server

import model._

object InMemService extends Api {
  private var memStorage: List[Todo] = Nil
  
  def store(data: List[Todo]): Unit = {
    println(s"Storing $data")
    memStorage = data
  }
  
  def load(): List[Todo] = {
    println(s"Loading $memStorage")
    memStorage
  }
}
