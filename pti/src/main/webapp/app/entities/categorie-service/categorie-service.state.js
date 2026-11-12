(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('categorie-service', {
            parent: 'entity', 
            url: '/categorie-service?page&sort&search',
            data: {  
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.serviceProfil.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/categorie-service/categorie-service.html',
                    controller: 'CategorieServiceController', 
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
        .state('categorie-service.new', {
            parent: 'categorie-service',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/categorie-service/categorie-service-dialog.html',
                    controller: 'CategorieServiceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                code: null,  
                                libelle: null,
                                ordre: null,
                               // typeServices:null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('categorie-service', null, { reload: 'categorie-service' });
                }, function() {
                    $state.go('categorie-service');
                });
            }]
        })
        /* .state('categorie-service.delete', {
            parent: 'categorie-service',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/categorie-service/categorie-service-delete-dialog.html',
                    controller: 'ParamCanalDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['CategorieService', function(CategorieService) {
                            return CategorieService.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('categorie-service', null, { reload: 'categorie-service' });
                }, function() {
                    $state.go('^');
                });
            }]
        }); */
    }

})();
