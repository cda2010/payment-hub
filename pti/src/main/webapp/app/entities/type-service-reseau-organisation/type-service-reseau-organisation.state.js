(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('type-service-reseau-organisation', {
            parent: 'entity', 
            url: '/type-service-reseau-organisation?page&sort&search',
            data: {  
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.serviceProfil.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/type-service-reseau-organisation/type-service-reseau-organisation.html',
                    controller: 'TypeServiceReseauOrganisationController', 
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
                    $translatePartialLoader.addPart('serviceProfil');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('type-service-reseau-organisation.new', {
            parent: 'type-service-reseau-organisation',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-service-reseau-organisation/type-service-reseau-organisation-dialog.html',
                    controller: 'TypeServiceReseauOrganisationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                code: null,  
                                libelle: null,
                                ordre: null,
                                typeServices:null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('type-service-reseau-organisation', null, { reload: 'type-service-reseau-organisation' });
                }, function() {
                    $state.go('type-service-reseau-organisation');
                });
            }]
        })
        /* .state('type-service-reseau-organisation.delete', {
            parent: 'type-service-reseau-organisation',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-service-reseau-organisation/type-service-reseau-organisation-delete-dialog.html',
                    controller: 'ParamCanalDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['typeServiceReseauOrganisation', function(typeServiceReseauOrganisation) {
                            return typeServiceReseauOrganisation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('type-service-reseau-organisation', null, { reload: 'type-service-reseau-organisation' });
                }, function() {
                    $state.go('^');
                });
            }]
        }); */
    }

})();
