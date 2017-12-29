package at.fhj.swengb.apps.battleship

import at.fhj.swengb.apps.battleship.BattleShipProtobuf.BattleShipGame.{Position, Vessel}
import at.fhj.swengb.apps.battleship.model.{NonEmptyString, _}

import scala.collection.JavaConverters._

object BattleShipProtocol {

  /**
    * Generates a Protobuf Position from given BattlePos position
    *
    * @param battlePos
    * @return
    */
  private def convert(battlePos: BattlePos): Position = {
    Position
      .newBuilder()
      .setColumn(battlePos.x)
      .setRow(battlePos.y)
      .build()
  }


  /** Converts a Protobuf Position to a BattleShipGame BattlePos
    *
    * @param position
    * @return
    */
  private def convert(position: Position): BattlePos = {
    BattlePos(position.getColumn, position.getRow)
  }

  
  /**
    * Converter: Vessel => Protobuf Vessel
    *
    * @param vessel
    * @return
    */
  def convert(vessel: at.fhj.swengb.apps.battleship.model.Vessel): Vessel = {
    Vessel.newBuilder()
      .setAlignment(vessel.direction match {
        case Vertical => "Vertical";
        case Horizontal => "Horizontal";
        case _ => "Vertical"
      })
      .setName(vessel.name.value)
      .setSize(vessel.size)
      .setPos(
        Position
          .newBuilder()
          .setRow(vessel.InitPosition.y)
          .setColumn(vessel.InitPosition.x)
          .build())
      .build()
  }

  /**
    * Converter: Protobuf Vessel => Vessel
    *
    * @param vessel
    * @return
    */
  def convert(vessel: Vessel): at.fhj.swengb.apps.battleship.model.Vessel = {
    at.fhj.swengb.apps.battleship.model.Vessel(
      NonEmptyString(vessel.getName),
      BattlePos(vessel.getPos.getColumn, vessel.getPos.getRow),
      vessel.getAlignment match {
        case "Vertical" => Vertical
        case "Horizontal" => Horizontal
        case _ => Vertical},
      vessel.getSize)
  }


  //Converter: Game State => Protobuf Game State
  def convert(game: BattleShipGame): BattleShipProtobuf.BattleShipGame = {
    val ProtoField3000 = BattleShipProtobuf.BattleShipGame
      .newBuilder()
      .setColumns(game.battleField.width)
      .setRows(game.battleField.height)

    //Converting the Vessels into Protobuf Format
    game.battleField.fleet.vessels.map(x => convert(x)).foreach(x => ProtoField3000.addVessels(x))

    //Converting the Cells that were already hit into Protobuf Format
    game.GameState.map(x => convert(x)).foreach(x => ProtoField3000.addHitCells(x))

    //Convert the Game State into Protobuf Format
    ProtoField3000.build()
  }


  //Converter: Protobuf Game State => Game State
  def convert(game: BattleShipProtobuf.BattleShipGame): BattleShipGame = {

    //Rebuilding the whole Game State
    val BattleShipGame3000 = BattleShipGame(
      BattleField(game.getColumns, game.getRows, Fleet(game.getVesselsList.asScala.map(e => convert(e)).toSet)),
      e => (), //Log
      e => (), //Slider
      e => e.toDouble, //CellWidth
      e => e.toDouble) //CellHeight

    //List with Cells that were already hit:
    val HitCells: List[BattlePos] = game.getHitCellsList.asScala.map(e => convert(e)).toList
    BattleShipGame3000.GameState = HitCells
    BattleShipGame3000 //Returning Game State
  }

}