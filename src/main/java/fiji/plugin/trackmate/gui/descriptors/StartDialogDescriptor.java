package fiji.plugin.trackmate.gui.descriptors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.gui.TrackMateGUIController;
import fiji.plugin.trackmate.gui.panels.StartDialogPanel;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import ij.ImagePlus;
import ij.WindowManager;

public class StartDialogDescriptor implements WizardPanelDescriptor
{

	private static final String KEY = "Start";

	private final StartDialogPanel panel;

	private final ArrayList< ActionListener > actionListeners = new ArrayList<>();

	private final TrackMateGUIController controller;

	/**
	 * The view that is launched immediately when this descriptor leaves. It
	 * will be used as a central view.
	 */
	private HyperStackDisplayer mainView;

	public StartDialogDescriptor( final TrackMateGUIController controller )
	{
		this.controller = controller;
		this.panel = new StartDialogPanel();
		panel.addActionListener( e -> fireAction( e ) );
	}

	/*
	 * METHODS
	 */

	/**
	 * Returns <code>true</code> if the {@link ImagePlus} selected is valid and
	 * can be processed.
	 *
	 * @return a boolean flag.
	 */
	public boolean isImpValid()
	{
		return panel.isImpValid();
	}

	/*
	 * WIZARDPANELDESCRIPTOR METHODS
	 */

	@Override
	public StartDialogPanel getComponent()
	{
		return panel;
	}

	@Override
	public void aboutToDisplayPanel()
	{
		controller.getGUI().setDisplayConfigButtonEnabled( false );
	}

	@Override
	public void displayingPanel()
	{
		ImagePlus imp;
		final TrackMate trackmate = controller.getPlugin();
		if ( null == trackmate.getSettings().imp )
		{
			imp = WindowManager.getCurrentImage();
		}
		else
		{
			panel.echoSettings( trackmate.getModel(), trackmate.getSettings() );
			imp = trackmate.getSettings().imp;
		}
		panel.getFrom( imp );
	}

	@Override
	public void aboutToHidePanel()
	{
		final TrackMate trackmate = controller.getPlugin();
		final Settings settings = trackmate.getSettings();
		final Model model = trackmate.getModel();

		/*
		 * Get settings and pass them to the trackmate managed by the wizard
		 */

		panel.updateTo( model, settings );
		trackmate.getModel().getLogger().log( settings.toStringImageInfo() );

		/*
		 * Configure settings object with spot, edge and track analyzers as
		 * specified in the providers.
		 */

		settings.addAllAnalyzers();
		trackmate.getModel().getLogger().log( settings.toStringFeatureAnalyzersInfo() );
		trackmate.computeSpotFeatures( true );
		trackmate.computeEdgeFeatures( true );
		trackmate.computeTrackFeatures( true );

		/*
		 * Launch the ImagePlus view now.
		 */

		// De-register old one, if any.
		if ( mainView != null )
		{
			mainView.clear();
			model.removeModelChangeListener( mainView );
		}

		final SelectionModel selectionModel = controller.getSelectionModel();
		mainView = new HyperStackDisplayer( model, selectionModel, settings.imp, controller.getDisplaySettings() );
		controller.getGuimodel().addView( mainView );
		mainView.render();
		controller.getGUI().setDisplayConfigButtonEnabled( true );
	}

	@Override
	public void comingBackToPanel()
	{}

	@Override
	public String getKey()
	{
		return KEY;
	}

	/*
	 * LISTERNER METHODS
	 */

	/**
	 * Adds an {@link ActionListener} to this panel. These listeners will be
	 * notified when a button is pushed or when the feature to color is changed.
	 */
	public void addActionListener( final ActionListener listener )
	{
		actionListeners.add( listener );
	}

	/**
	 * Removes an ActionListener from this panel.
	 *
	 * @return true if the listener was in the ActionListener collection of this
	 *         instance.
	 */
	public boolean removeActionListener( final ActionListener listener )
	{
		return actionListeners.remove( listener );
	}

	public Collection< ActionListener > getActionListeners()
	{
		return actionListeners;
	}

	/**
	 * Forward the given {@link ActionEvent} to all the {@link ActionListener}
	 * of this panel.
	 */
	private void fireAction( final ActionEvent e )
	{
		for ( final ActionListener l : actionListeners )
		{
			l.actionPerformed( e );
		}
	}

}
