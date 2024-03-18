package org.historyeraser.workers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.historyeraser.ConfiguredChannel;
import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.json.simple.JSONArray;

public class RemoveChannelWorker
{
	private static final Type typeToken = new TypeToken<List<ConfiguredChannel>>()
	{
	}.getType();

	public CompletableFuture<String> execute(DiscordApi api, List<SlashCommandInteractionOption> arg)
	{
		return CompletableFuture.supplyAsync(() -> {
			try
			{
				boolean worked = false;
				Gson gson = new Gson();
				StringBuilder fileContentStr = new StringBuilder();

				File myObj = new File("historyremover.json");
				Scanner myReader = new Scanner(myObj);
				while (myReader.hasNextLine())
				{
					fileContentStr.append(myReader.nextLine());
				}
				myReader.close();
				List<ConfiguredChannel> json = gson.fromJson(fileContentStr.toString(), typeToken);
				JSONArray jsonArray = new JSONArray();
				ConfiguredChannel configuredChannel = new ConfiguredChannel();
				for (int i = 0; i < arg.size(); i++)
				{
					SlashCommandInteractionOption interactionOption = arg.get(i);

					if (interactionOption.getName().equals("channel"))
					{
						configuredChannel.textchannelid = interactionOption.getChannelValue().get().getIdAsString();
					}
				}

				for (ConfiguredChannel configuredChannel1 : json)
				{
					if (configuredChannel1.textchannelid.equals(configuredChannel.textchannelid))
					{
						worked = true;
						continue;
					}
					jsonArray.add(configuredChannel1);
				}

				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("historyremover.json", false));
				bufferedWriter.write(gson.toJson(jsonArray));
				bufferedWriter.newLine();
				bufferedWriter.flush();
				bufferedWriter.close();

				if (worked)
				{
					return "Successfully removed eraser to new channel";
				}
				else
				{
					return "Failed to remove eraser to new channel";
				}

			}
			catch (Throwable t)
			{
				throw new CompletionException(t.getMessage(), t);
			}
		}, api.getThreadPool().getExecutorService());
	}
}



