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
import org.historyeraser.Channels;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

public class HistoryEraserWorker
{
	private static final Type typeToken = new TypeToken<List<Channels>>()
	{
	}.getType();
	DiscordApi api;

	public HistoryEraserWorker(DiscordApi api)
	{
		this.api = api;
	}

	public CompletableFuture<ArrayList<Stream<Message>>> execute(DiscordApi api)
	{
		ArrayList<Stream<Message>> arrayList = new ArrayList<>();

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

				List<Channels> json = gson.fromJson(fileContentStr.toString(), typeToken);

				for (Channels channels : json)
				{
					String channelid = channels.textchannelid;
					long hours = channels.hours;
					Optional<TextChannel> textChannel = api.getTextChannelById(channelid);
					if (textChannel.isPresent())
					{
						TextChannel tc = textChannel.get();
						Instant nHoursAgo = Instant.now().minus(hours, ChronoUnit.HOURS);
						Stream<Message> messageStream = tc.getMessagesAsStream()
							.filter(m -> m.getCreationTimestamp()
								.isBefore(nHoursAgo) && m.canYouDelete()
							)
//							.sorted((m1, m2) -> (m1.getCreationTimestamp().compareTo(m2.getCreationTimestamp())))
							.limit(50);

						arrayList.add(messageStream);
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
}
