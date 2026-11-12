(function () {
    'use strict';

    angular
            .module('ptiApp')
            .controller('ReglementOrganisationController', ReglementOrganisationController);

    ReglementOrganisationController.$inject = ['$state','Principal',  'ReglementOrganisation', 'ParseLinks', 'Utils', 'BASE_URL_API', 'AlertService', 'paginationConstants', 'Notification', 'pagingParams', '$uibModal', 'RequestUtils', 'CancelAlert'];

    function ReglementOrganisationController($state,Principal,  ReglementOrganisation, ParseLinks, Utils, BASE_URL_API, AlertService, paginationConstants, Notification, pagingParams, $uibModal, RequestUtils, CancelAlert) {

        var vm = this;
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.canShowResultCount = false;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.addReglement = addReglement;
        vm.voirLigneRegl = voirLigneRegl;
        vm.validerReglement = validerReglement;
        vm.voirReglement = voirReglement;
        vm.idSelectedCode=null;
        vm.estAnnuler=false;
        vm.enTraitement=false;
        vm.imprimerRapportReglmt = imprimerRapportReglmt;
        vm.confirmAnnulation = confirmAnnulation;
        vm.annulerReglement=annulerReglement;
        if ($state.current.name==='reglement-organisation-valide'){
            console.log('yesssssss');
        }
        vm.idRegl = null;
        vm.ctrlValide = true;
        var URL = BASE_URL_API + 'etat/export/rapportReglement';
        loadAll();
        vm.operation = {
            id: null
        };
        vm.item = {
            reglementId: null
        };
        function loadAll() {

            if ($state.current.name==='reglement-organisation-valide'){
                vm.afficheButtonValider=true;
                vm.afficheButtonAjout=false;
                console.log('valide');
            }else{
                vm.afficheButtonValider=false;
                vm.afficheButtonAjout=true;
                console.log('initiation');
            }
            vm.canShowResultCount = true;
            ReglementOrganisation.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.reglements = data;
                console.log(data);
                vm.page = pagingParams.page;
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadPage(page) {
            vm.page = page;
            vm.transition();
        }
        function addReglement() {
            $uibModal.open({
                templateUrl: 'app/entities/reglement/modal-reglement/reglement-organisation-modal.html',
                controller: 'ReglementOrganisationDialogController',
                controllerAs: 'vm',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return {
                            codeRetrait: null,
                            uoAcquereurId: null,
                            canalLibelleCanal: null,
                            code: null,
                            id: null
                        };
                    },
                    isViewing: function () {
                        return false;
                    }
                }
            }).result.then(function () {
                console.log('toi');
                // $state.go('reglement-organisation');
                $state.reload("reglement-organisation");
            }, function () {
                console.log('Moi');
            })

        }
        function voirLigneRegl(id) {
            console.log('-----Id------', id);
            $uibModal.open({
                templateUrl: 'app/entities/reglement/ligne-reglement/ligne-reglement-modal.html',
                controller: 'LigneReglementModalController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
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
                    entity: function () {
                        return id;
                    }
                }

            })
        }

        function voirReglement(reglement) {
            vm.idSelectedCode=reglement.id;
            if ($state.current.name==='reglement-organisation-valide'){
                vm.ctrlValide = reglement.statut;
                vm.estAnnuler=reglement.estAnnuler;
                vm.enTraitement=reglement.enTraitement;
            }
            console.log(reglement);
            console.log("vm.estAnnuler",vm.estAnnuler);
            vm.idRegl = reglement.id;
            console.log("fsdfc",vm.idReg);
        }
        function confirmAnnulation(reglement) {
            if(reglement.estAnnuler===false ||reglement.estAnnuler===null) {
            CancelAlert.confirm(function () {
                 annulerReglement(reglement.id)
                        .success(function (datas) {
                            console.log('Yes', datas);
                            Notification.success('Annulation éffectuée avec succès');
                            $state.reload('reglement-organisation-valide');
                        })
                        .error(function (datas) {
                            console.log('No', datas);
                            Notification.error('Echec de l\'annulation');
                            $state.reload('reglement-organisation-valide');
                        })
                 })
            } else {
                Notification.error('Réglement déja annuler');
            }
        }
        vm.traitementOperationReglement = function () {
            console.log('il');
            vm.operation.id=vm.idRegl;
            Principal.identity().then(function (account) {
                vm.operation.login = account.login;
            });
            $uibModal.open({
                templateUrl: 'app/entities/operation/reglement-organisation-validation/reglement-traitement.html',
                controller: 'ReglementTraitementController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return vm.operation;
                    }
                }
            }).result.then(function () {
                //$uibModalInstance.close(true);
                $state.reload('reglement-organisation-valide');
            });
        };

        function annulerReglement(reglementId) {
            console.log('id',reglementId);
            ReglementOrganisation.annulationReglement({reglementId:reglementId}, function (result) {
                console.log(result);
                Notification.success('Annulation éffectuée avec succès');
                $state.reload('reglement-organisation-valide');
            });
            $state.reload('reglement-organisation-valide');
        }

        function validerReglement() {
            console.log('id', vm.idRegl);
            vm.operation.id = vm.idRegl;

            Principal.identity().then(function (account) {
                vm.operation.login = account.login;
            });

            RequestUtils.OperationReglementCompte.save(vm.operation, function (result) {
                console.log(result);
                Notification.success('Validation effectuée');
            })
        }


        function imprimerRapportReglmt(reglementId) {
            vm.item.reglementId = reglementId;
            Utils.postPrintUrl(URL, vm.item);
        }

        function transition() {
            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch
            });
        }
    }
})();
