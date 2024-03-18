package org.historyeraser.workers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;
import org.historyeraser.ConfiguredChannel;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;

public class HistoryEraserWorker
{
	private static final Type typeToken = new TypeToken<List<ConfiguredChannel>>()
	{
	}.getType();
	DiscordApi api;

	public HistoryEraserWorker(DiscordApi api)
	{
		this.api = api;
	}

	public CompletableFuture<ArrayList<CompletableFuture<MessageSet>>> execute(DiscordApi api)
	{
		ArrayList<CompletableFuture<MessageSet>> arrayList = new ArrayList<>();

		return CompletableFuture.supplyAsync(() -> {
			try
			{
				StringBuilder fileContentStr = new StringBuilder();
				Gson gson = new Gson();

				File myObj = new File("historyremover.json");
				Scanner myReader = new Scanner(myObj);
				while (myReader.hasNextLine())
				{
					fileContentStr.append(myReader.nextLine());
				}
				myReader.close();

				List<ConfiguredChannel> json = gson.fromJson(fileContentStr.toString(), typeToken);

				for (ConfiguredChannel configuredChannel : json)
				{
					String channelid = configuredChannel.textchannelid;
					long hours = configuredChannel.hours;
					Optional<TextChannel> textChannel = api.getTextChannelById(channelid);
					if (textChannel.isPresent())
					{
						TextChannel tc = textChannel.get();
						Instant nHoursAgo = Instant.now().minus(hours, ChronoUnit.HOURS);

						arrayList.add(tc.getMessagesBefore(50, getCreationTimestamp(nHoursAgo)));
					}
				}
			}
			catch (Throwable t)
			{
				throw new CompletionException(t.getMessage(), t);
			}
			return arrayList;
		}, api.getThreadPool().getExecutorService());
	}
	static Long getCreationTimestamp(Instant timeStampTime) {
		 return (timeStampTime.toEpochMilli() - 1420070400000L) << 22;
	}
}
