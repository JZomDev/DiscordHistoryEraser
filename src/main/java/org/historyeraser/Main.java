package org.historyeraser;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.historyeraser.listeners.AddChannels;
import org.historyeraser.listeners.RemoveChannels;
import org.historyeraser.listeners.ServerBecomesAvailable;
import org.historyeraser.workers.HistoryEraserWorker;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;

public class Main
{

	private static final Logger logger = LogManager.getLogger(Main.class);

	public static String DISCORD_TOKEN = "";
	private static ScheduledExecutorService mService;
	private static DiscordApi discordApi;

	/**
	 * The entrance point of our program.
	 *
	 * @param args The arguments for the program. The first element should be the bot's token.
	 */
	public static void main(String[] args) throws Exception
	{
		DISCORD_TOKEN = args[0];
		if (DISCORD_TOKEN.equals(""))
		{
			logger.error("Failed to start Discord bot. No Discord token supplied");
		}
		else
		{
			DiscordApiBuilder builder = new DiscordApiBuilder();
			builder.setAllIntents();
			builder.setToken(DISCORD_TOKEN);
			builder.setTrustAllCertificates(false);
			builder.setWaitForServersOnStartup(false);
			builder.setWaitForUsersOnStartup(false);
			builder.addServerBecomesAvailableListener(new ServerBecomesAvailable());

			SlashCommandsSetUp slashCommandsSetUp = new SlashCommandsSetUp();

			discordApi = builder.login().join();

			discordApi.bulkOverwriteGlobalApplicationCommands(slashCommandsSetUp.getCommands()).join();
			discordApi.addSlashCommandCreateListener(new AddChannels());
			discordApi.addSlashCommandCreateListener(new RemoveChannels());
			logger.info("You can invite me by using the following url: " + discordApi.createBotInvite());
			HistoryEraserWorker worker = new HistoryEraserWorker(discordApi);
			launchScheduledExecutor(worker);
		}
	}


	public static void launchScheduledExecutor(HistoryEraserWorker worker)
	{
		if (mService == null || mService.isShutdown())
		{
			mService = Executors.newScheduledThreadPool(1);

		}
		mService.scheduleAtFixedRate(() -> {
				// Perform your recurring method calls in here.
				try
				{
					ArrayList<CompletableFuture<Void>> completableFutureArrayList = new ArrayList<>();
					ArrayList<CompletableFuture<MessageSet>> streamArrayList = worker.execute(discordApi).join();
					for (int j = 0; j < streamArrayList.size(); j++)
					{
						CompletableFuture<MessageSet> messages = streamArrayList.get(j);
						messages.whenComplete((messageSet, throwable) -> {
							if (throwable != null)
							{
								logger.error(throwable.getMessage(), throwable);
							}
							else if (messageSet.size() > 0)
							{
								logger.info("Found {} messages to delete, deleting them", messageSet.size());
								completableFutureArrayList.add(messageSet.deleteAll());
							}
						});
					}

					// this is effectively await
					for (CompletableFuture<Void> completableFuture : completableFutureArrayList)
					{
						completableFuture.join();
					}
				}
				catch (Exception e)
				{
					// don't stop process, just log the error and try again
					logger.error(e.getMessage(), e);
				}
			},
			0, // How long to delay the start
			30, // How long between executions
			TimeUnit.SECONDS); // The time unit used
	}
}