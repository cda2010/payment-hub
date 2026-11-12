(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('terminal', {
            parent: 'entity',
            url: '/terminal',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.terminal.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/terminal/terminals.html',
                    controller: 'TerminalController',
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
                    $translatePartialLoader.addPart('terminal');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        });
       
        
    }

})();
