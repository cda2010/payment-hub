(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RetraitDialogController', RetraitDialogController);

    RetraitDialogController.$inject = ['$http','$sessionStorage','isMaj','$uibModalInstance', '$state', 'Utils', 'BASE_URL', 'isViewing', 'CancelAlert', 'OperationUtils', 'Notification', 'TypePiece', 'Nationalite', 'RequestUtils', '$scope', 'entity'];

    function RetraitDialogController($http,$sessionStorage,isMaj,$uibModalInstance, $state, Utils, BASE_URL, isViewing, CancelAlert, OperationUtils, Notification, TypePiece, Nationalite, RequestUtils, $scope, entity) {
        var vm = this;
        vm.isViewing = isViewing;
        vm.clear = clear;
        vm.isMaj = isMaj;
        vm.url = BASE_URL + "api/reporting/operations/bordereau";
        vm.verification = verification;
        vm.nationalites = Nationalite.query();
        vm.typepieces = TypePiece.query();
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.isOperationValide = false;
        vm.operation = entity;
        vm.codeRetraitHash = null;
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
        vm.personneB = {
            telephone: null,
            typePersonne: 'PHYSIQUE'
        };
        vm.infos = {};

        function clear() {
            //Mise à jour de l'operation annule
            if (angular.isDefined(vm.infos.operationDTO) && vm.infos.operationDTO != null) {
                RequestUtils.OperationAnnulationRetraitCash.update(vm.infos.operationDTO, function () {
                    console.log("Annulation d'une opération de retrait");
                    $state.reload("operation-retrait");
                });
            }
            if (vm.isViewing)
                $uibModalInstance.close(true);
            else
                $state.reload("operation-envoi-cash");
        }

        function openCalendar(date) {
            console.log("open");
            vm.datePickerOpenStatus[date] = true;
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

        function save() {
            CancelAlert.confirm(function () {
                console.log("validation du retrait");
                var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
            $http.jsonp(url)
                .success(function (data) {
                    RequestUtils.VerifToken.get({
                        token: vm.token,
                        loginData: JSON.stringify(data)
                    }, function (datas) {
                        vm.infos.beneficiaire.personneDTO = vm.personneB;
                        vm.infos.beneficiaire.pieceDTO = vm.pieceB;
                        vm.infos.operationDTO.canalRetrait = "WEB";
                        vm.infos.typeService = "RETRAIT";
                        RequestUtils.OperationVerifCumul.save(vm.infos, function (result) {
                            if (result != null) {
                                switch (result.action) {
                                    case "BLOQUE":
                                        Notification.error(result.message + "!");
                                        break;
                                    case "NON_BLOQUANT":
                                        CancelAlert.confirm(function () {
                                            datas.operationDTO.statut = "ENATTENTE_VALIDATION";
                                            saveNow();
                                        }, function () {
                                        }, result.message + "\n Soumettre pour validation?");
                                        break;
                                    default: saveNow();
                                        break;
                                }
                            } else {
                                saveNow();
                            }
                        });
                    });
                }).error(function () {
                Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
            });


            });
        }

        function saveNow() {
            RequestUtils.OperationRetraitCash.save(vm.infos, function (result) {
                Notification.success("Retrait effectué !");
                Utils.postPrintUrl(vm.url, { 'operationId': result.id });
                $state.reload("operation-retrait");
            }, function () {
                console.log("echec de l'operation");
            });
        }
        OperationUtils.init($scope, vm, "RETRAIT", vm.operation);

        vm.doOperationRedressement = function () {
            CancelAlert.confirm(function () {
                RequestUtils.doOperationRedressement.save(vm.operation, function () {
                    Notification.success("Redressement de l'operation effectué!");
                    $uibModalInstance.close(true);
                    $state.go('home');
                }, function () {
                    vm.isSaving = false;
                });
            });
        }
    }
})();
