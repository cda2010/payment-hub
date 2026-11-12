(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UniteOrganisationEditDialogController', UniteOrganisationEditDialogController);

    UniteOrganisationEditDialogController.$inject = ['ServiceProfil','Ville','Commerciale','PieceByTitulaireId', 'RequestUtils', '$timeout', 'Nationalite', 'TypePiece', 'PersonnePhysique', '$q', 'PersonneMorale','$scope', 'DateUtils', '$uibModalInstance', 'entity', 'UniteOrganisation', 'TableauFrais', 'Compte', 'Autorisation', 'PointVente', 'TypeUniteOrganisation', 'Personne', 'ProfilActeur'];

    function UniteOrganisationEditDialogController(ServiceProfil,Ville,Commerciale, PieceByTitulaireId, RequestUtils, $timeout, Nationalite, TypePiece, PersonnePhysique, $q, PersonneMorale, $scope, DateUtils, $uibModalInstance, entity, UniteOrganisation, TableauFrais, Compte, Autorisation, PointVente, TypeUniteOrganisation, Personne, ProfilActeur) {
        var vm = this;

        vm.uniteOrganisation = entity;

        vm.typepieces = TypePiece.query();
        vm.nationalites = Nationalite.query();
        vm.seviceProfiles=ServiceProfil.query();
        Ville.query({
            uoId: entity.pereUOId
        }, function (datas) {
            vm.villes = datas;
        });
        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus = {};
        vm.isViewing = false;
        vm.isEdit = true;
        vm.clear = clear;
        vm.save = save;
        //vm.uniteorganisations = UniteOrganisation.query();
        vm.tableaufrais = TableauFrais.query();
        vm.autorisations = Autorisation.query();
        vm.pointventes = PointVente.query();
        vm.profils = ProfilActeur.query();
        vm.typeuniteorganisations = TypeUniteOrganisation.queryOptimisetUo();
        vm.commerciaux=Commerciale.query();

        if (vm.uniteOrganisation.dateCreation)
            vm.uniteOrganisation.dateCreation = DateUtils.convertLocalDateFromServer(vm.uniteOrganisation.dateCreation);


        RequestUtils.GetFournisseur.query({}, function (datas) {
            console.log('Request to get FSE');
            vm.fournisseurservices = datas;
        });
        vm.typeUOChange = typeUOChange;
        vm.niveau = null;

        function typeUOChange() {
            TypeUniteOrganisation.get({
                id: vm.uniteOrganisation.typeUOId
            }, function (data) {
                vm.niveau = data.niveau;
            }, function () {});
        }



        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        if (vm.isEdit || vm.isViewing) {
            TypeUniteOrganisation.get({
                id: vm.uniteOrganisation.typeUOId
            }, function (data) {
                vm.niveau = data.niveau;
            }, function () {});
            if (vm.uniteOrganisation.representantId != null) {
                vm.uniteOrganisation.rep = true;
                Personne.get({
                    id: vm.uniteOrganisation.representantId
                }, function (data) {
                    vm.personneE = data;
                    if (vm.personneE.typePersonne == "PHYSIQUE") {
                        PersonnePhysique.get({
                            id: data.personnePhysiqueId
                        }, function (data) {
                            vm.personnePhysiqueE = data;
                        }, function () {});
                    }
                    if (vm.personneE.typePersonne == "MORALE") {
                        PersonneMorale.get({
                            id: data.personneMoraleId
                        }, function (data) {
                            vm.personneMoraleE = data;
                        }, function () {});
                    }
                    PieceByTitulaireId.get({
                        id: data.id
                    }, function (data) {
                        vm.pieceE = data;
                    }, function () {});
                }, function () {});
            }

        }

        function save() {
            vm.isSaving = true;
            if (vm.uniteOrganisation.rep) {
                var emetteur = {};
                emetteur.personneDTO = vm.personneE;
                if (vm.personneE.typePersonne == "PHYSIQUE") {
                    emetteur.personnePhysiqueDTO = vm.personnePhysiqueE;
                } else {
                    emetteur.personneMoraleDTO = vm.personneMoraleE;
                }
                emetteur.pieceDTO = vm.pieceE;
                vm.uniteOrganisation.personneAllDTO = emetteur;
            } else {
                vm.uniteOrganisation.personneAllDTO = null;
            }
            if (vm.uniteOrganisation.codeFse != null) {
                var fse = _.find(vm.fournisseurservices, {
                    'codeFse': vm.uniteOrganisation.codeFse
                });
                vm.uniteOrganisation.codeBanque = fse.codeExterne;
            }
            if (vm.uniteOrganisation.id !== null) {
                UniteOrganisation.update(vm.uniteOrganisation, onSaveSuccess, onSaveError);
            } else {
                UniteOrganisation.save(vm.uniteOrganisation, onSaveSuccess, onSaveError);
            }
        }


        function onSaveSuccess(result) {
            $scope.$emit('ptiApp:uniteOrganisationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }
        vm.datePickerOpenStatus.dateCreation = false;

        function openCalendar(date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
