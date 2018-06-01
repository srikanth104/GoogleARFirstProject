package com.android.findheart

import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

/**
 * Created by Srikanth on 31/05/18.
 */
class KotlinMainActivity : AppCompatActivity() {


    lateinit var fragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            addHeartObject(Uri.parse("Heart.sfb"));
        }
    }

    private fun addHeartObject(parse: Uri) {

        val frame = fragment.arSceneView.arFrame;
        val point = getScreenCenter();
        if (frame != null) {
            val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat());
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    placeObject(fragment, hit.createAnchor(), parse);
                    break;
                }
            }
        }
    }

    private fun placeObject(fragment: ArFragment, createAnchor: Anchor, model: Uri) {
        ModelRenderable.builder()
                .setSource(fragment.context, model)
                .build()
                .thenAccept {
                    addNodeToScene(fragment, createAnchor, it)
                }
                .exceptionally {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(it.message)
                            .setTitle("error!")
                    val dialog = builder.create()
                    dialog.show()
                    return@exceptionally null
                }
    }

    private fun addNodeToScene(fragment: ArFragment, createAnchor: Anchor, renderable: ModelRenderable?) {
        val anchorNode = AnchorNode(createAnchor)
        val rotatingNode = RotatingHeart()
        val tranformableNode = TransformableNode(fragment.transformationSystem)
        rotatingNode.renderable = renderable
        rotatingNode.addChild(tranformableNode)
        rotatingNode.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        tranformableNode.select()
    }


    private fun getScreenCenter(): Point {
        val viewValue = findViewById<View>(android.R.id.content);
        return Point(viewValue.width / 2, viewValue.height / 2);
    }

}