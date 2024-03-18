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

public class AddChannelWorker
{
	private static final Type typeToken = new TypeToken<List<ConfiguredChannel>>()
	{
	}.getType();

	public CompletableFuture<String> execute(DiscordApi api, List<SlashCommandInteractionOption> arg)
	{
		return CompletableFuture.supplyAsync(() -> {
			try
			{

				ConfiguredChannel newConfiguredChannel = new ConfiguredChannel();
				for (int i = 0; i < arg.size(); i++)
				{
					SlashCommandInteractionOption interactionOption = arg.get(i);

					switch (interactionOption.getName())
					{
						case "channel":
							newConfiguredChannel.textchannelid = interactionOption.getChannelValue().get().getIdAsString();
							break;
						case "hours":
							newConfiguredChannel.hours = interactionOption.getLongValue().get().longValue();
							break;
					}
				}

				Gson gson = new Gson();
				StringBuilder fileContentStr = new StringBuilder();
				JSONArray jsonArray = new JSONArray();

				File myObj = new File("historyremover.json");
				if (myObj.exists())
				{
					Scanner myReader = new Scanner(myObj);
					while (myReader.hasNextLine())
					{
						fileContentStr.append(myReader.nextLine());
					}
					myReader.close();
					List<ConfiguredChannel> json = gson.fromJson(fileContentStr.toString(), typeToken);

					for (ConfiguredChannel configuredChannel : json)
					{
						if (newConfiguredChannel.textchannelid.equals(configuredChannel.textchannelid))
						{
							continue;
						}
						jsonArray.add(configuredChannel);
					}
				}

				jsonArray.add(newConfiguredChannel);

				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("historyremover.json", false));
				bufferedWriter.write(gson.toJson(jsonArray));
				bufferedWriter.newLine();
				bufferedWriter.flush();
				bufferedWriter.close();

				return "Successfully added eraser to new channel";
			}
			catch (Throwable t)
			{
				throw new CompletionException(t.getMessage(), t);
			}
		}, api.getThreadPool().getExecutorService());
	}
}



