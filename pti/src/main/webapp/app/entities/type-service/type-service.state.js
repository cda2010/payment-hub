(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('type-service', {
            parent: 'entity',
            url: '/type-service?page&sort&search',
            data: {
                authorities: ['ROLE_CRE_TYPE_SERVICE'],
                pageTitle: 'ptiApp.typeService.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/type-service/type-services.html',
                    controller: 'TypeServiceController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('typeService');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
;
    }

})();
