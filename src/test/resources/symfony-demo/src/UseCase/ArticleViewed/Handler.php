<?php

namespace App\UseCase\ArticleViewed;


use App\Infrastructure\Bus\Handler\DomainEventHandler;
use Psr\Log\LoggerInterface;
use Symfony\Component\DependencyInjection\Attribute\Autowire;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;

class Handler implements DomainEventHandler
{
    public function __construct(
        private LoggerInterface $logger,
        private readonly MailerInterface $mailer,
        #[Autowire('%app.notifications.email_sender%')]
        private readonly string $emailSender,
    )
    {
    }

    public function __invoke(Event $event):void
    {
        $this->logger->info("Article {id} viewed",['id'=>$event->id]);
        $email = (new Email())
            ->from($this->emailSender)
            ->to($this->emailSender)
            ->subject(\sprintf('Article %s viewed', $event->id))
            ->text($this->emailSender);

        $this->mailer->send($email);
    }
}
