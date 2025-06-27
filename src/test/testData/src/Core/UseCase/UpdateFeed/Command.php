<?php

declare(strict_types=1);

namespace App\Core\UseCase\UpdateFeed;

use App\Core\Id\FeedId;
use App\Infrastructure\Bus\Message\Command\AsyncCommandInterface;

final readonly class Command implements AsyncCommandInterface
{
    public function __construct(public FeedId $feedId)
    {
    }
}
