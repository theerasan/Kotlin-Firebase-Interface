import express.ExpressApp
import express.Request
import express.Response
import extention.Object
import extention.asModel
import firebase.FirebassAppExample
import model.SongExample

class SongServiceExample(firebaseApp: FirebassAppExample) {

    private val api = express.ExpressExample(require("express")).api
    val database = firebaseApp.database

    fun getApi(): ExpressApp {
        api.get("/:id/details/", getSong())
        api.get("/list/", getSongs())
        api.put("/create/", createSong())
        api.post(":id/update/", updateSong())
        api.delete(":id/delete/", deleteSong())
        return api
    }

    private fun createSong(): (Request, Response) -> Unit = { req, res ->
        val key = database.ref("songs").push().key ?: ""
        val song = req.body.asModel<SongExample>()
        song.key = key
        database.ref("songs/$key")
                .set(song)
                .then {
                    res.status(200).send("${song.title} by ${song.artist} has been created")
                }.catch {
                    res.status(500).send(it.message ?: "")
                }
    }

    private fun getSong(): (Request, Response) -> Unit = { req, res ->
        val id = req.param("id")
        database.ref("songs/$id")
                .once("value")
                .then {
                    if (!it.exists()) {
                        res.status(404).send("Song id: $id not found")
                        return@then
                    }
                    val song = it.`val`<SongExample>()
                    res.status(200).send(song)
                }
                .catch {
                    res.status(500).send(it.message ?: "")
                }
    }

    private fun getSongs(): (Request, Response) -> Unit = { req, res ->
        database.ref("songs")
                .once("value")
                .then {
                    val songs: List<SongExample> = Object.values(it.`val`())
                    res.status(200).send(songs)
                }
                .catch {
                    res.status(500).send(it.message ?: "")
                }
    }

    private fun updateSong(): (Request, Response) -> Unit = { req, res ->

    }

    private fun deleteSong(): (Request, Response) -> Unit = { req, res ->

    }
}