<?php

declare(strict_types=1);

namespace App\UserInterface\Web\Api\Article\Models;

use App\Core\Id\ArticleId;
use App\Core\Query\ArticleDTO;
use App\Core\Query\FeedDTO;
use App\UserInterface\Web\Api\Feed\Models\Feed;
use Brick\DateTime\LocalDateTime;
use Nelmio\ApiDocBundle\Annotation\Model;
use OpenApi\Attributes as OA;

#[OA\Schema(required: [
    'id',
    'url',
    'title',
    'description',
    'keywords',
    'tags',
    'feeds',
    'authors',
    'alternates',
    'publishDate',
    'languages',
    'image',
    'favicon',
    'canonical_link',
    'createdAt',
    'updatedAt',
    'associated',
    'isBookmark',
    'isViewed',
    'isLiked',
    'isRead',
])]
final readonly class Article
{
    public function __construct(
        public ArticleId $id,
        public string $url,
        public string $title,
        public string $description,
        #[OA\Property(type: 'array', items: new OA\Items(type: 'string'))]
        public array $keywords,
        #[OA\Property(type: 'array', items: new OA\Items(type: 'string'))]
        public array $tags,
        #[OA\Property(type: 'array', items: new OA\Items(ref: new Model(type: Feed::class)))]
        public array $feeds,
        #[OA\Property(type: 'array', items: new OA\Items(type: 'string'))]
        public array $authors,
        #[OA\Property(type: 'array', items: new OA\Items(type: 'string'))]
        public array $alternates,
        #[OA\Property(type: 'datetime')]
        public LocalDateTime $publishDate,
        #[OA\Property(type: 'array', items: new OA\Items(type: 'string'))]
        public array $languages,
        public ?string $image,
        public ?string $favicon,
        public ?string $canonical_link,
        #[OA\Property(type: 'datetime')]
        public LocalDateTime $createdAt,
        #[OA\Property(type: 'datetime')]
        public LocalDateTime $updatedAt,
        public bool $associated = false,
        public bool $isBookmark = false,
        public bool $isViewed = false,
        public bool $isLiked = false,
        public bool $isRead = false,
    ) {
    }

    public static function from(ArticleDTO $articleDTO): Article
    {
        return new Article(
            $articleDTO->id,
            $articleDTO->url,
            $articleDTO->title,
            $articleDTO->description,
            $articleDTO->keywords,
            $articleDTO->tags,
            array_map(fn (FeedDTO $feed) => Feed::from($feed), $articleDTO->feeds),
            $articleDTO->authors,
            $articleDTO->alternates,
            $articleDTO->publishDate,
            $articleDTO->languages,
            $articleDTO->image,
            $articleDTO->favicon,
            $articleDTO->canonical_link,
            $articleDTO->createdAt,
            $articleDTO->updatedAt,
            $articleDTO->associated,
            $articleDTO->isBookmark,
            $articleDTO->isViewed,
            $articleDTO->isLiked,
            $articleDTO->isRead,
        );
    }
}
