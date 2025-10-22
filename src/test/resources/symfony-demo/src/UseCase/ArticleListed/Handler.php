<?php

namespace App\UseCase\ArticleListed;


use App\Infrastructure\Bus\Handler\DomainEventHandler;
use Psr\Log\LoggerInterface;

class Handler implements DomainEventHandler
{
    public function __construct(
        private LoggerInterface $logger
    )
    {
    }

    public function __invoke(Event $event):void
    {
        $this->logger->info("Article {id} Listed",['id'=>$event->id]);
    }
}
