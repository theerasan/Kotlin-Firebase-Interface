import express.ExpressApp
import express.Request
import express.Response
import extention.Object
import extention.asModel
import firebase.FirebassAppExample
import model.SongExample
import kotlin.coroutines.experimental.*
import kotlin.js.Promise

class SongServiceExample(firebaseApp: FirebassAppExample) {

    private val api = express.ExpressExample().api
    val database = firebaseApp.database

    fun getApi(): ExpressApp {
        api.get("/:id/details/", getSong())
        api.get("/:artist/list", getSongsByArtist())
        api.get("/list/", getSongs())
        api.put("/create/", createSong())
        api.post("/:id/update/", updateSong())
        api.delete("/:id/delete/", deleteSong())
        return api
    }

    private fun getSongsByArtist(): (Request, Response) -> Unit = { req, res ->
        val artist = req.params.artist as String
        val songs = ArrayList<SongExample>()

        val ref = database.ref("songs")
                .orderByChild("artist")
                .equalTo(artist)
                .limitToFirst(10)

        ref.on("child_added") { snapshot, _ ->
            console.log(snapshot.`val`())
            songs.add(snapshot.`val`())
            ref.off()
        }.also { _ ->
            ref.once("value")
                    .then {
                        res.status(200).send(songs)
                    }
        }
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

    private fun getSongs(): (Request, Response) -> Unit = { _, res ->
        database.ref("songs")
                .once("value")
                .then {
                    val songs: Array<SongExample> = Object.values(it.`val`())
                    res.status(200).send(songs)
                }
                .catch {
                    res.status(500).send(it.message ?: "")
                }
    }

    private fun updateSong(): (Request, Response) -> Unit = { req, res ->
        val id = req.param("id")
        val song = req.body.asModel<SongExample>()
        database.ref("songs/$id")
                .update(song)
                .then {
                    res.status(200).send("Song id: $id has been updated")
                }
                .catch {
                    res.status(500).send(it.message ?: "")
                }
    }

    private fun deleteSong(): (Request, Response) -> Unit = { req, res ->
        val id = req.param("id")
        database.ref("songs/$id")
                .remove()
                .then {
                    res.status(200).send("Song id: $id has been deleted")
                }
                .catch {
                    res.status(500).send(it.message ?: "")
                }
    }
}
