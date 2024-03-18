package org.historyeraser.listeners;

import java.util.concurrent.CompletableFuture;
import org.historyeraser.workers.RemoveChannelWorker;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

public class RemoveChannels implements SlashCommandCreateListener
{
	@Override
	public void onSlashCommandCreate(SlashCommandCreateEvent event)
	{
		SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
		RemoveChannelWorker removeChannelWorker = new RemoveChannelWorker();

		if (slashCommandInteraction.getCommandName().equals("removechannel"))
		{
			CompletableFuture<String> stringCompletableFuture = removeChannelWorker.execute(event.getApi(), slashCommandInteraction.getArguments());
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
