package com.example.hashi

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


// ================ Resources =====================
// https://www.tutorialspoint.com/how-to-draw-a-line-in-android-using-kotlin
// https://stackoverflow.com/questions/2902640/android-get-the-screen-resolution-pixels-as-integer-values
// https://developer.android.com/reference/android/widget/TableLayout
// https://stackoverflow.com/a/24079302 // dynamic table rows/cols
// https://stackoverflow.com/a/57686965 // check light/dark mode

class MainActivity : AppCompatActivity() {

    private val gameBoard = HashiBoard()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        boardSetup(this)
    }

    private fun boardSetup(context: Context) {
        // initiate the actual game board content
        gameBoard.createGameBoard()
        Log.i("Main_boardSetup", "Successfully created game board")
        val nodes = gameBoard.nodeMap
        val nMap = gameBoard.boardByIdentifiers

        // helper function to check if device is using dark mode/theme
        fun isDark(activity: MainActivity) : Boolean {
            Log.i("Main_boardSetup", "Checking if dark mode")
            // gives error: Attempt to invoke virtual method on null object reference
            return false
//            return activity.resources.configuration.uiMode and
//                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        }

        val gameTable : TableLayout = findViewById(R.id.tableLayout)
        for (r in 0 until gameBoard.numRows) {
            Log.i("Main_boardSetup", "Creating new row")
            val boardTableRow = TableRow(context)
            boardTableRow.id = (r * 100 + 100) // ids of 100, 200, 300, ...
            for (c in 0 until gameBoard.numCols) {
                val imgAtGrid = ImageView(context)
                imgAtGrid.id = (r * 100 + c + 101) // ids of 101, 102, ..., 201, ...

                // determine appropriate image content for grid position
                val gridLocId = nMap[r][c]
                Log.i("Main_boardSetup", "Grid location id: $gridLocId")
                var drawableId : Int = R.drawable.hashi_is0_256grey
                if (gridLocId < 0) { // bridge or blank
                    Log.i("Main_boardSetup", "Placing blank tile at location")
                    drawableId = R.drawable.hashi_blankgrid
                }
                else {
                    // get node
                    Log.i("Main_boardSetup", "Attempting to locate node at $r $c")
                    val rcNode = nodes.find { it.isLoc.contentEquals(intArrayOf(c, r)) }
                    if (rcNode != null) {
                        Log.i("Main_boardSetup", "Assigning island ${rcNode.isIdentifier} to r${r}c${c}")
                        drawableId = if (isDark(MainActivity())) {
                            rcNode.isDisplayOnDark
                        } else {
                            rcNode.isDisplayOnLight
                        }
                    }
//                    else if (it.isLoc[0] != c || ) {
//                        //
//                    }
                    else {
                        Log.i("Main_boardSetup", "----Node not at r${r}c${c}")
                        // R.drawable.hashi_is0_256grey
                    }
                    imgAtGrid.isClickable = true
                    imgAtGrid.setOnClickListener { view ->
                        if (rcNode != null) {
                            selectIsland(view, rcNode)
                        }
                    }
                }

                // assign drawable to view
                imgAtGrid.setImageResource(drawableId)

                // add child view (table column) to the row
                boardTableRow.addView(imgAtGrid)
            }

            // add child view row to the table
            gameTable.addView(boardTableRow)
        }
    } // end function: boardSetup

    // handle when player selects an island
    // case 1: none selected -> first selection
    // case 2: selected same island as first selection -> deselect
    // case 3: selected neighboring island to first selection -> attempt bridge
    // case 4: selected non-neighboring island to first selection -> reselect
    private fun selectIsland (view: View, node: HashiNode) {
        if (node.isSelected) {
            Snackbar.make(view, "Cleared selection", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            node.isSelected = false
            return
        }

        // search all nodes for one that is already selected
        for (n in gameBoard.nodeMap) {
            if (n.isSelected) {
                // check if it is a neighbor
                for ((dir, nb) in node.neighbors.withIndex()) {
                    if (n.isIdentifier == nb) {
                        Snackbar.make(view, "Selected neighbor", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show()
                        // dirs: N E S W
                        // node is neighbor to a selected node
                        // get direction as string
                        // attempt to draw bridge
                        node.drawBridge(when (dir) {
                            0 -> "N"
                            1 -> "E"
                            2 -> "S"
                            3 -> "W"
                            else -> ""
                        }, n )

                        // place bridge drawables along path
                        return
                    }
                }

                // node is not a neighbor: reselect
                Snackbar.make(view, "Selected new node", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                n.isSelected = false
                node.isSelected = true

                return
            }
        }

        // no nodes selected
        Snackbar.make(view, "Selected node as first selection", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        node.isSelected = true

    }
}