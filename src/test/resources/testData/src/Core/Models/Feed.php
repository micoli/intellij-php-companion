<?php

declare(strict_types=1);

namespace App\Core\Models;

use App\Core\Id\ArticleId;
use App\Infrastructure\Persistence\Doctrine\TimestampableTrait;
use Brick\DateTime\LocalDateTime;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;


#[ORM\Entity]
#[ORM\Table(name: 'article__feed')]
#[ORM\UniqueConstraint(name: 'article__unique_article_url', columns: ['url'])]
#[ORM\HasLifecycleCallbacks]
class Feed
{
    use TimestampableTrait;

    #[ORM\Id]
    #[ORM\Column(type: 'article_id')]
    #[ORM\GeneratedValue(strategy: 'NONE')]
    private ArticleId $id;

    #[ORM\Column(type: 'string', nullable: false)]
    private string $url;

    #[ORM\Column(type: 'string', nullable: false)]
    private string $title;

    public function __construct(
        string $url,
        string $title,
    ) {
        $this->id = ArticleId::create();
        $this->url = $url;
        $this->title = $title;
    }

    public function getId(): ArticleId
    {
        return $this->id;
    }

    public function getUrl(): string
    {
        return $this->url;
    }

    public function getTitle(): string
    {
        return $this->title;
    }
}