package fiji.plugin.trackmate;

import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.TrackMateGUIController;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettingsIO;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class TrackMatePlugIn implements PlugIn
{

	protected TrackMate trackmate;

	protected Settings settings;

	protected Model model;

	protected DisplaySettings displaySettings;

	/**
	 * Runs the TrackMate GUI plugin.
	 *
	 * @param imagePath
	 *            a path to an image that can be read by ImageJ. If set, the
	 *            image will be opened and TrackMate will be started set to
	 *            operate on it. If <code>null</code> or 0-length, TrackMate
	 *            will be set to operate on the image currently opened in
	 *            ImageJ.
	 */
	@Override
	public void run( final String imagePath )
	{
		final ImagePlus imp;
		if ( imagePath != null && imagePath.length() > 0 )
		{
			imp = IJ.openImage( imagePath );
			if ( null == imp.getOriginalFileInfo() )
			{
				IJ.error( TrackMate.PLUGIN_NAME_STR + " v" + TrackMate.PLUGIN_NAME_VERSION, "Could not load image with path " + imagePath + "." );
				return;
			}
		}
		else
		{
			imp = WindowManager.getCurrentImage();
			if ( null == imp )
			{
				IJ.error( TrackMate.PLUGIN_NAME_STR + " v" + TrackMate.PLUGIN_NAME_VERSION, "Please open an image before running TrackMate." );
				return;
			}
		}
		imp.setOpenAsHyperStack( true );
		imp.setDisplayMode( IJ.COMPOSITE );
		if ( !imp.isVisible() )
			imp.show();

		GuiUtils.userCheckImpDimensions( imp );

		settings = createSettings( imp );
		model = createModel();
		final SelectionModel selectionModel = new SelectionModel( model );
		trackmate = createTrackMate();
		displaySettings = createDisplaySettings();

		/*
		 * Launch GUI.
		 */

		final TrackMateGUIController controller = new TrackMateGUIController( trackmate, displaySettings, selectionModel );
		GuiUtils.positionWindow( controller.getGUI(), imp.getWindow() );
	}

	/*
	 * HOOKS
	 */

	protected DisplaySettings createDisplaySettings()
	{
		return DisplaySettingsIO.readUserDefault();
	}

	/**
	 * Hook for subclassers: <br>
	 * Creates the {@link Model} instance that will be used to store data in the
	 * {@link TrackMate} instance.
	 *
	 * @return a new {@link Model} instance.
	 */

	protected Model createModel()
	{
		return new Model();
	}

	/**
	 * Hook for subclassers: <br>
	 * Creates the {@link Settings} instance that will be used to tune the
	 * {@link TrackMate} instance. It is initialized by default with values
	 * taken from the current {@link ImagePlus}.
	 *
	 * @param imp
	 *            the {@link ImagePlus} to operate on.
	 * @return a new {@link Settings} instance.
	 */
	protected Settings createSettings( final ImagePlus imp )
	{
		final Settings lSettings = new Settings();
		lSettings.setFrom( imp );
		return lSettings;
	}

	/**
	 * Hook for subclassers: <br>
	 * Creates the TrackMate instance that will be controlled in the GUI.
	 *
	 * @return a new {@link TrackMate} instance.
	 */
	protected TrackMate createTrackMate()
	{
		/*
		 * Since we are now sure that we will be working on this model with this
		 * settings, we need to pass to the model the units from the settings.
		 */
		final String spaceUnits = settings.imp.getCalibration().getXUnit();
		final String timeUnits = settings.imp.getCalibration().getTimeUnit();
		model.setPhysicalUnits( spaceUnits, timeUnits );

		return new TrackMate( model, settings );
	}

	/*
	 * MAIN METHOD
	 */

	public static void main( final String[] args )
	{
		ImageJ.main( args );
//		new TrackMatePlugIn().run( "samples/Stack.tif" );
//		new TrackMatePlugIn().run( "samples/Merged.tif" );
		new TrackMatePlugIn().run( "samples/MAX_Merged.tif" );
//		new TrackMatePlugIn().run( "samples/Mask.tif" );
//		new TrackMatePlugIn().run( "samples/FakeTracks.tif" );
	}

}
