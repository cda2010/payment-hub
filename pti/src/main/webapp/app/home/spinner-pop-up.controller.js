(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('SpinnerPopUpController',SpinnerPopUpController);

    SpinnerPopUpController.$inject = ['$http', '$uibModalInstance', '$uibModal', '$state', '$rootScope', 'Notification','RequestUtils','Operation','BASE_URL', 'CancelAlert', '$scope', '$stateParams'];

    function SpinnerPopUpController($http, $uibModalInstance, $uibModal, $state, $rootScope, Notification, RequestUtils, Operation,BASE_URL,  CancelAlert, $scope, $stateParams) {
        var vm = this;

        vm.clear = clear;

        vm.save = save;
        //InitiateEasyPieChart.init();
        //InitiateSparklineCharts.init();
        vm.statuts = ["PAYE", "ENATTENTE", "ENATTENTE_VALIDATION", "ANNULLE", "PAYE", "SUSPENDU", "REJETE", "VALIDE", 'REMBOURSE'];


        vm.options = {
            legend: {
                display: true,
                position: 'left'
            }
        };
        vm.labelsPeriode=[];
        vm.dataPeriode=[65, 59, 80, 81, 56, 55, 40, 23, 64, 51, 38, 26];
        vm.initCanvasData=initCanvasData;
        vm.periode={
            value:'today',
            displayName:'Aujourd\'hui',
            group:'---Par heure---'
        };
        vm.periodes=[
            {
                value:'today',
                displayName:'Aujourd\'hui',
                group:'---Par heure---'
            },
            {
                value:'yesterday',
                displayName:'Hier',
                group:'---Par heure---'
            },
            {
                value:'7days',
                displayName:'Les 7 derniers jours',
                group:'---Par jour---'
            },
            {
                value:'14days',
                displayName:'Les 14 derniers jours',
                group:'---Par jour---'
            },
            {
                value:'30days',
                displayName:'Les 30 derniers jours',
                group:'---Par jour---'
            },

            {
                value:'thisweek',
                displayName:'Cette semaine',
                group:'---Par jour---'
            },
            {
                value:'lastweek',
                displayName:'La semaine dernière',
                group:'---Par jour---'
            },
            {
                value:'thismonth',
                displayName:'Ce mois',
                group:'---Par jour---'
            },
            {
                value:'lastmonth',
                displayName:'Le mois dernier',
                group:'---Par jour---'
            },
            {
                value:'12month',
                displayName:'Les 12 derniers mois',
                group:'---Par mois---'
            },
            {
                value:'thisyear',
                displayName:'Cette année',
                group:'---Par mois---'
            },
            {
                value:'lastyear',
                displayName:'L\'année dernière',
                group:'---Par mois---'
            },
            {
                value:'5year',
                displayName:'Les 5 dernières années',
                group:'---Par année---'
            },
        ];


        $scope.possibleTimes= [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23];




        /**
         Recherche du tableau de change
         */
        startDashBoard();

        /*setTimeout(function () {
            $scope.$apply(function(){
                startDashBoard();
            },function () {
                console.log('ff',vm.dataOp);
                $uibModalInstance.close(true);
            },function () {
                $uibModalInstance.close(true);
            });
        }, 2000);*/



        function clear() {
            vm.isSaving = false;
            $uibModalInstance.close(true);
            if (vm.isViewing)
                $uibModalInstance.close(true);
            else
                $state.reload("home");
        }


        function save() {

        }
        /*function init() {
            Operation.query({
                "size": 10,
                "statutOperation": vm.statutOperation
            }, function (datas) {
                vm.operations = datas;
                console.log('op-------------',vm.operations);
            });

            RequestUtils.GetOrganisationByType.get({
                type: "PAYS"
            }, function (datas) {
                vm.pays = datas;
            });
        }*/
        function initCanvasData(){
            console.log('1.2');
            vm.labelsPeriode=[];
            vm.dataPeriode=[];
            if(vm.periode.group=='---Par heure---'){
                Operation.queryByHour({periode:vm.periode.value},function (result) {
                    var i;

                    for (i=0;i<24;i++){
                        vm.labelsPeriode.push(result[i].label);
                        vm.dataPeriode.push(result[i].value);
                    }
                },function (error) {
                    Notification.warning("Opération échouée");
                })
            }else if(vm.periode.group=='---Par jour---') {
                console.log('Par jour');
                //vm.labelsPeriode=["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"];
                Operation.queryByDay({periode:vm.periode.value},function (result) {
                    var i;
                    console.log('Par result',result);
                    for (i=0;i<result.length;i++){
                        vm.labelsPeriode.push(result[i].label);
                        vm.dataPeriode.push(result[i].value);
                    }
                },function (error) {
                    Notification.warning("Opération échouée");
                })
            }else if(vm.periode.group=='---Par mois---') {
                //vm.labelsPeriode=["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"];
                Operation.queryByMonth({periode:vm.periode.value},function (result) {
                    var i;
                    for (i=0;i<result.length;i++){
                        vm.labelsPeriode.push(result[i].label);
                        vm.dataPeriode.push(result[i].value);
                    }
                },function (error) {
                    Notification.warning("Opération échouée");
                })
            }else{

                Operation.queryByYear(function (result) {
                    var i;

                    for (i=0;i<result.length;i++){
                        vm.labelsPeriode.push(result[i].label);
                        vm.dataPeriode.push(result[i].value);
                    }
                },function (error) {
                    Notification.warning("Opération échouée");
                });

            }
            console.log('canvas--',vm.labelsPeriode,vm.dataPeriode)

        }

        function startDashBoard() {
            initCanvasData();
            //init();
            RequestUtils.BilanOp.get({}, function (datas) {
                console.log('1.1');
                vm.labelsOp = datas.labels;
                vm.dataOp = datas.datas;
                $uibModalInstance.close(true);
            });

        }

    }
})();
