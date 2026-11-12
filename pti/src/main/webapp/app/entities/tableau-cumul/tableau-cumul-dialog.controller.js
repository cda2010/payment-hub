(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TableauCumulDialogController', TableauCumulDialogController);

    TableauCumulDialogController.$inject = ['DateUtils', '$scope', 'RequestUtils', '$uibModalInstance', 'entity', 'TableauCumul', 'TypeService', 'Canal', 'Client'];

    function TableauCumulDialogController (DateUtils, $scope, RequestUtils, $uibModalInstance, entity, TableauCumul, TypeService, Canal, Client) {
        var vm = this;
        vm.tableauCumul = entity;
        console.log(vm.tableauCumul);
        vm.clear=clear;
        vm.save = save;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.typeservices = TypeService.queryAll();
        vm.canals = Canal.query();
        vm.clients = Client.query();
        vm.type=['PAYS','FSE'] ;
        vm.typeOrganisationEm = [] ;
        vm.typeOrganisationAcq = [] ;

        vm.typeUoEmmetteur = null ;
        vm.typeUoAcq = null ;


        vm.init = function(){
            if(vm.tableau!==null && vm.tableauCumul.organisationEmId!==null)
            {
                RequestUtils.getTypeUoByUoId.get({ id: vm.tableauCumul.organisationEmId }, function (datas) {
                    vm.typeUoEmmetteur = datas.libelleTypeUO;
                });
            }
            if(vm.tableau!==null && vm.tableauCumul.organisationAcqId!==null)
            {
                RequestUtils.getTypeUoByUoId.get({ id: vm.tableauCumul.organisationAcqId }, function (datas) {
                    vm.typeUoAcq = datas.libelleTypeUO;
                });
            }
        }
        vm.init() ;

        if (vm.tableauCumul.id != null) {
            vm.tableauCumul.dateDebut = DateUtils.convertLocalDateFromServer(vm.tableauCumul.dateDebut);
            vm.tableauCumul.dateFin = DateUtils.convertLocalDateFromServer(vm.tableauCumul.dateFin);
        }
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        $scope.$watch("vm.typeUoEmmetteur", function (newValues, oldValues) {
            var type = newValues;
           
            if(type==='PAYS')
            {
                RequestUtils.GetOrganisationByType.get({type: "PAYS"}, function (datas) {
                    console.log("Reponse organisations", datas);
                    vm.typeOrganisationEm = datas;
                });
            }
            else
            {
                RequestUtils.GetOrganisationByType.get({type: "FSE"}, function (datas) {
                    vm.typeOrganisationEm = datas;
                });
            }
        });

        $scope.$watch("vm.typeUoAcq", function (newValues, oldValues) {
            var type = newValues;
            if(type==='PAYS')
            {
                RequestUtils.GetOrganisationByType.get({type: "PAYS"}, function (datas) {
                    console.log("Reponse organisations", datas);
                    vm.typeOrganisationAcq = datas;
                });
            }
            else
            {
                RequestUtils.GetOrganisationByType.get({type: "FSE"}, function (datas) {
                    vm.typeOrganisationAcq = datas;
                });
            }
        });
      
        
        function save () {
            vm.isSaving = true;
            if (vm.tableauCumul.id!==null) {
                TableauCumul.update(vm.tableauCumul, onSaveSuccess, onSaveError);
            }else{
                TableauCumul.save(vm.tableauCumul, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:tableauCumulUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateDebut = false;
        vm.datePickerOpenStatus.dateFin = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }

    }
})();
