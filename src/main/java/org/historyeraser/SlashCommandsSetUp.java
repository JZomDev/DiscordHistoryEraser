package org.historyeraser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOption;

public class SlashCommandsSetUp
{
	public Set<SlashCommandBuilder> getCommands()
	{

		Set<SlashCommandBuilder> builders = new HashSet<>();

		ArrayList<ChannelType> channelTypeArrayList = new ArrayList<>();
		channelTypeArrayList.add(ChannelType.SERVER_TEXT_CHANNEL);
		channelTypeArrayList.add(ChannelType.SERVER_NEWS_CHANNEL);
		builders.add(new SlashCommandBuilder().setName("addchannel")
			.setDescription("Add a channel to be cleaned up")
			.setOptions(Arrays.asList(
				SlashCommandOption.createChannelOption("channel", "channels to clean up", true, channelTypeArrayList),
				SlashCommandOption.createLongOption("hours", "how many hours old to delete", true)

			))
			.setDefaultEnabledForPermissions(PermissionType.ADMINISTRATOR)
		);
		builders.add(new SlashCommandBuilder().setName("removechannel")
			.setDescription("Remove a channel to be cleaned up")
			.setOptions(Collections.singletonList(
				SlashCommandOption.createChannelOption("channel", "channels to clean up", true, channelTypeArrayList)
			))
			.setDefaultEnabledForPermissions(PermissionType.ADMINISTRATOR)
		);

		return builders;
	}
}
