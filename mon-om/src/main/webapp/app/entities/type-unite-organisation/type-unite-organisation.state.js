(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('type-unite-organisation', {
            parent: 'entity',
            url: '/type-unite-organisation',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'TypeUniteOrganisations'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/type-unite-organisation/type-unite-organisations.html',
                    controller: 'TypeUniteOrganisationController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('type-unite-organisation-detail', {
            parent: 'type-unite-organisation',
            url: '/type-unite-organisation/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'TypeUniteOrganisation'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/type-unite-organisation/type-unite-organisation-detail.html',
                    controller: 'TypeUniteOrganisationDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'TypeUniteOrganisation', function($stateParams, TypeUniteOrganisation) {
                    return TypeUniteOrganisation.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'type-unite-organisation',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('type-unite-organisation-detail.edit', {
            parent: 'type-unite-organisation-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-unite-organisation/type-unite-organisation-dialog.html',
                    controller: 'TypeUniteOrganisationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['TypeUniteOrganisation', function(TypeUniteOrganisation) {
                            return TypeUniteOrganisation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('type-unite-organisation.new', {
            parent: 'type-unite-organisation',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-unite-organisation/type-unite-organisation-dialog.html',
                    controller: 'TypeUniteOrganisationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('type-unite-organisation', null, { reload: 'type-unite-organisation' });
                }, function() {
                    $state.go('type-unite-organisation');
                });
            }]
        })
        .state('type-unite-organisation.edit', {
            parent: 'type-unite-organisation',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-unite-organisation/type-unite-organisation-dialog.html',
                    controller: 'TypeUniteOrganisationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['TypeUniteOrganisation', function(TypeUniteOrganisation) {
                            return TypeUniteOrganisation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('type-unite-organisation', null, { reload: 'type-unite-organisation' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('type-unite-organisation.delete', {
            parent: 'type-unite-organisation',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/type-unite-organisation/type-unite-organisation-delete-dialog.html',
                    controller: 'TypeUniteOrganisationDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['TypeUniteOrganisation', function(TypeUniteOrganisation) {
                            return TypeUniteOrganisation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('type-unite-organisation', null, { reload: 'type-unite-organisation' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
