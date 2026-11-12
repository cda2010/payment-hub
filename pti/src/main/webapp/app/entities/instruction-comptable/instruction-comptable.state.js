(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('instruction-comptable', {
            parent: 'entity',
            url: '/instruction-comptable?page&sort&search',
            data: {
                authorities: ['ROLE_CRE_INSTRUCTION_COMPT'],
                pageTitle: 'ptiApp.instructionComptable.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/instruction-comptable/instruction-comptables.html',
                    controller: 'InstructionComptableController',
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
                    $translatePartialLoader.addPart('instructionComptable');
                    $translatePartialLoader.addPart('instructionEcriture');
                    $translatePartialLoader.addPart('montantsAComptabiliser');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        });
    }

})();
