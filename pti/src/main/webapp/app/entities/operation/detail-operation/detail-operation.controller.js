(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailOperationController', DetailOperationController);

    DetailOperationController.$inject = ['$http','$sessionStorage','$uibModalInstance','$uibModal', '$state', 'Utils', 'BASE_URL', 'isViewing', 'CancelAlert', 'OperationUtils', 'Notification', 'TypePiece', 'Nationalite', 'RequestUtils', 'InitSteps','$scope', 'typeService','entity'];

    function DetailOperationController($http,$sessionStorage,$uibModalInstance,$uibModal, $state, Utils, BASE_URL, isViewing, CancelAlert, OperationUtils, Notification, TypePiece, Nationalite, RequestUtils,InitSteps, $scope, typeService,entity) {
        var vm = this;
        vm.isViewing = isViewing;
        vm.clear = clear;
        vm.url = BASE_URL + "api/reporting/operations/bordereau";
        vm.verification = verification;
        vm.nationalites = Nationalite.query();
        vm.typepieces = TypePiece.query();
        vm.datePickerOpenStatus = {};
        vm.isOperationValide = false;
        vm.operation=entity;
        vm.typeService=typeService;
        vm.codeRetraitHash = null;
        vm.validerOperationAnnulation=validerOperationAnnulation;
        console.log('--1',vm.operation,'--2',vm.typeService);
        vm.fseSrc = {
            id: null
        };
        vm.fseDest = {
            id: null
        };
        vm.pieceB = {
            referencePiece: null,
            dateDelivrance: null,
            dateExpiration: null,
            copie: null,
            copieContentType: null,
            lieuDelivrance: null,
            id: null
        };
        vm.canVerif = true;
        vm.personneE = {
            typePersonne: 'PHYSIQUE'
        };
        vm.personneB = {
            telephone: null,
            typePersonne: 'PHYSIQUE'
        };
        vm.compte = {
            estCompteBanque: null,
            estCompteAutre: null
        };
        vm.oldUoAcquereurId;
        vm.oldUoEmetteurId;
        vm.infos = {};
        vm.steps = [
            'Information',
            'Expéditeur',
            'Bénéficiaire'
        ];
        if (!vm.isViewing) {
            vm.steps.push("Confirmation");
        }
        var datas = {
            vm: vm,
            steps: vm.steps
        };

        vm.beneficiaireInfos = {
            compteCBIDTO: {},
            clientDTO: {},
            personneDTO: {
                typePersonne: null
            },
            personneMoraleDTO: {},
            personnePhysiqueDTO: {}
        };

        vm.emetteurInfos = {
            compteCBIDTO: {},
            clientDTO: {},
            personneDTO: {},
            personneMoraleDTO: {},
            personnePhysiqueDTO: {}
        };
        RequestUtils.UserOrganisation.get({}, function (data) {
            console.log("organisation de l'utilisateur");
            vm.userOrganisation = data;
        });

        vm.estCompteBanque = true;

        InitSteps.initSteps(datas);


        OperationUtils.init($scope, vm, vm.typeService, $uibModalInstance);
        OperationUtils.initVerificationOrganisatioType(vm);
        vm.oldUoAcq=null;
        vm.oldUoEm=null;
        function clear() {
                $uibModalInstance.close(true);
                $state.go("operation");
        }


        vm.annulationOperationAvecFrais = function (avecFrais) {
            CancelAlert.confirm(function () {
                RequestUtils.AnnulationAccountAvecRetourFrais.get({
                    operationId: vm.operation.id,
                    avecFrais: avecFrais
                }, function (datas) {
                    if (datas != null) {
                        console.log('redressement s frais',datas);
                        vm.operation = datas;
                        $uibModalInstance.close(true);
                        Notification.success("Remboursement effectué avec succès!");
                        //$state.reload("operation");

                    }
                });
            });
        };


        vm.annulationOperationSansFrais = function () {
            CancelAlert.confirm(function () {
                RequestUtils.AnnulationEnvoiCashSansRetourFrais.get({
                    operationId: vm.operation.id
                }, function (datas) {
                    if (datas != null) {
                        console.log('redressement av frais',datas);
                        vm.operation = datas;
                        $uibModalInstance.close(true);
                        Notification.success("Remboursement effectué avec succès!");
                        //$state.reload("operation");
                    }
                });
            });
        };


        function validerOperationAnnulation(typeFrais) {
            console.log('typeFrais',typeFrais);
                $uibModal.open({
                    templateUrl: 'app/entities/operation/detail-operation/detail-operation-traitement.html',
                    controller: 'DetailOperationTraitementController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return vm.operation;
                        },
                        frais: function () {
                            return typeFrais;
                        }
                    }
                }).result.then(function () {
                    $uibModalInstance.close(true);
                });
            //$uibModalInstance.close();
        }


        /*
            Verification de l'existence d'une operation  de retrait
            En utilisant le code et le montant de l'operation
        */
        function verification() {
            console.log("verification de l'operation");
            vm.isOperationValide = false;
            RequestUtils.VerificationOperation.get({
                codeRetrait: vm.codeRetrait
            }, function (datas) {
                if (datas != null) {
                    console.log("operation validée!");
                    vm.personneB = datas.beneficiaire.personneDTO;
                    if (datas.beneficiaire.personnePhysiqueDTO != null) {
                        vm.personnePhysiqueB = datas.beneficiaire.personnePhysiqueDTO;
                    } else {
                        vm.personneMoraleB = datas.beneficiaire.personneMoraleDTO;
                    }
                    vm.operation = datas.operationDTO;
                    vm.infos = datas;
                    vm.infos.beneficiaire.pieceDTO = null;
                    vm.isOperationValide = true;
                    vm.canVerif = false;
                }
            });
        }



    }
})();
