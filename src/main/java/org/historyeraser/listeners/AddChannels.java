package org.historyeraser.listeners;

import java.util.concurrent.CompletableFuture;
import org.historyeraser.workers.AddChannelWorker;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

public class AddChannels implements SlashCommandCreateListener
{

	@Override
	public void onSlashCommandCreate(SlashCommandCreateEvent event)
	{
		SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
		AddChannelWorker addChannelWorker = new AddChannelWorker();

		if (slashCommandInteraction.getCommandName().equals("addchannel"))
		{
			CompletableFuture<String> stringCompletableFuture = addChannelWorker.execute(event.getApi(), slashCommandInteraction.getArguments());
			slashCommandInteraction.respondLater(true)
				.thenAccept(interactionOriginalResponseUpdater -> stringCompletableFuture.whenComplete((str, e) -> {
					if (str.length() > 0)
					{
						slashCommandInteraction.createFollowupMessageBuilder()
							.setContent(str).send();
					}
					else
					{
						slashCommandInteraction.createFollowupMessageBuilder().setFlags(MessageFlag.EPHEMERAL)
							.setContent("Something went wrong").send();
					}
				}));
		}
	}
}
