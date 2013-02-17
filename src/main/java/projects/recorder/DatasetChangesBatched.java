package projects.recorder;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Quad ;

public abstract class DatasetChangesBatched implements DatasetChanges
{
    // Extract the state machine ?
    private QuadAction currentAction    = null ;
    private Node currentSubject         = null ;
    private Node currentGraph           = null ;
    private List<Quad>   batchQuads     = null ;

    @Override public final void start()
    {
        startBatch() ;
        startBatched() ;
    }

    @Override public final void finish()
    {
        finishBatch() ;
        finishBatched() ;
    }

    @Override
    public void change(QuadAction qaction, Node g, Node s, Node p, Node o)
    {
        if ( ! Lib.equal(currentAction, qaction) ||
            ! Lib.equal(currentGraph, g) ||
            ! Lib.equal(currentSubject, s) )
        {
            finishBatch() ;
            startBatch() ;
            currentAction = qaction ;
            currentGraph = g ;
            currentSubject = s ;
        }
        
        batchQuads.add(new Quad(g,s,p,o)) ;
    }
    
    private void startBatch()
    {
        if ( batchQuads == null )
            batchQuads = new ArrayList<>() ;
    }

    protected void finishBatch()
    {
        if ( batchQuads == null || batchQuads.size() == 0 )
            return ;
        dispatch(batchQuads) ;
        batchQuads = null ;
    }

    protected abstract void dispatch(List<Quad> batch) ;

    protected abstract void startBatched() ;

    protected abstract void finishBatched() ;

}

